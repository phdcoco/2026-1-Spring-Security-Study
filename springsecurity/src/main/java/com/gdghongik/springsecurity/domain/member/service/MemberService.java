package com.gdghongik.springsecurity.domain.member.service;

import com.gdghongik.springsecurity.domain.member.dto.MemberCreateRequest;
import com.gdghongik.springsecurity.domain.member.dto.MemberInfoResponse;
import com.gdghongik.springsecurity.domain.member.dto.MemberUpdateRequest;
import com.gdghongik.springsecurity.domain.member.entity.Member;
import com.gdghongik.springsecurity.domain.member.repository.MemberRepository;
import com.gdghongik.springsecurity.global.exception.CustomException;
import com.gdghongik.springsecurity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.gdghongik.springsecurity.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void createMember(MemberCreateRequest request) {
        // 중복되는 유저네임이 있으면 에러
        // DTO를 record로 바꿨으면 getUsername()이 아닌 그냥 username()이다. 필드명이 메서드명이다.
        boolean isDuplicate = memberRepository.existsByUsername(request.username());

        if (isDuplicate) {
            throw new CustomException(MEMBER_USERNAME_DUPLICATE);
        }

        Member member = new Member(request.username(), request.password());

        // 멤버를 저장한다
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<MemberInfoResponse> getMembers() {
        // 모든 멤버를 가져온다
        List<MemberInfoResponse> list = new ArrayList<>();

        List<Member> members = memberRepository.findAll(); // JpaRepository에 이미 findAll()이 선언되어 있음.

        for (Member member : members) {
            MemberInfoResponse response = MemberInfoResponse.from(member);
            list.add(response);
        }

        return list;
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberByUsername(String username) {
        // 해당하는 멤버를 가져온다. 없으면 에러
        Member member = memberRepository.findByUsername(username);

        if (member == null) {
            throw new CustomException(MEMBER_NOT_FOUND);
        }

        return MemberInfoResponse.from(member);
    }

    @Transactional
    public void updateMember(Long memberId, MemberUpdateRequest request) {
        // 해당하는 멤버를 가져온다. 없으면 에러
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        // 해당하는 멤버 정보를 갱신한다
        member.updateUsername(request.username());
    }

    @Transactional
    public void deleteMember(Long memberId) {
        // 해당하는 멤버를 가져온다. 없으면 에러
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        // 해당 멤버를 삭제한다
        memberRepository.delete(member);
    }
}
