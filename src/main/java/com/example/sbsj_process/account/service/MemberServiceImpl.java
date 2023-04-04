package com.example.sbsj_process.account.service;

import com.example.sbsj_process.account.entity.Authentication;
import com.example.sbsj_process.account.entity.BasicAuthentication;
import com.example.sbsj_process.account.entity.Member;
import com.example.sbsj_process.account.entity.MemberProfile;
import com.example.sbsj_process.account.repository.AuthenticationRepository;
import com.example.sbsj_process.account.repository.MemberProfileRepository;
import com.example.sbsj_process.account.repository.MemberRepository;
import com.example.sbsj_process.account.request.MemberLoginRequest;
import com.example.sbsj_process.account.request.MemberRegisterRequest;
import com.example.sbsj_process.account.response.MemberLoginResponse;
import com.example.sbsj_process.security.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    final private MemberRepository memberRepository;
    final private MemberProfileRepository memberProfileRepository;
    final private AuthenticationRepository authenticationRepository;
    final private RedisService redisService;

    @Override
    public Boolean signUp(MemberRegisterRequest memberRegisterRequest) {
        final Member member = memberRegisterRequest.toMember();
        final MemberProfile memberProfile = memberRegisterRequest.toMemberProfile(member);

        try{
            memberRepository.save(member);
            memberProfileRepository.save(memberProfile);
        } catch(Exception e) {
            memberRepository.deleteByMemberNo(member.getMemberNo());
            return false;
        }

        final BasicAuthentication authentication = new BasicAuthentication(
                member,
                Authentication.BASIC_AUTH,
                memberRegisterRequest.getPassword()
        );

        authenticationRepository.save(authentication);
        return true;
    }

    @Override
    public Boolean idValidation(String id) {
        Optional<Member> maybeMember = memberRepository.findById(id);

        if (maybeMember.isPresent()) {
            return false;
        }

        return true;
    }

    @Override
    public Boolean emailValidation(String email) {
        Optional<MemberProfile> maybeMemberProfile = memberProfileRepository.findByEmail(email);

        if (maybeMemberProfile.isPresent()) {
            return false;
        }

        return true;
    }

    @Override
    public Boolean phoneNumberValidation(String phoneNumber) {
        Optional<MemberProfile> maybeMemberProfile = memberProfileRepository.findByPhoneNumber(phoneNumber);

        if (maybeMemberProfile.isPresent()) {
            return false;
        }

        return true;
    }

    @Override
    public MemberLoginResponse signIn(MemberLoginRequest memberLoginRequest) {
        Optional<Member> maybeMember = memberRepository.findById(memberLoginRequest.getId());

        MemberLoginResponse memberLoginResponse = new MemberLoginResponse();

        if(maybeMember.isPresent()) {
            Member member = maybeMember.get();
            memberLoginResponse.setMemberNo(member.getMemberNo());

            if(!member.isRightPassword(memberLoginRequest.getPassword())) {
                memberLoginResponse.setToken("incorrect");
                return memberLoginResponse;
            }

            UUID userToken = UUID.randomUUID();

            // redis 처리 필요
            redisService.deleteByKey(userToken.toString());
            redisService.setKeyAndValue(userToken.toString(), member.getMemberNo());

            memberLoginResponse.setToken(userToken.toString());
            return memberLoginResponse;
        }

        memberLoginResponse.setToken("no");
        return memberLoginResponse;
    }

    @Override
    public void delete(Long memberNo) {
        System.out.println("서비스에서 보는 delete memberNo: "+ memberNo);
        Optional<Member> maybeMember = memberRepository.findByMemberNo(memberNo);

        if(maybeMember.isEmpty()) {
//            System.out.println("서비스에서 보는 delete: "+ maybeMember.get());
            System.out.println("서비스에서 없어 그냥 없어.");
            return;
        }

        if(maybeMember.isPresent()) {
            Member member = maybeMember.get();
            memberProfileRepository.deleteByMember(member);
            memberRepository.deleteByMemberNo(memberNo);
        }

    }



}