package com.gdghongik.springsecurity.domain.member.controller;

import com.gdghongik.springsecurity.domain.member.dto.MemberCreateRequest;
import com.gdghongik.springsecurity.domain.member.dto.MemberInfoResponse;
import com.gdghongik.springsecurity.domain.member.dto.MemberUpdateRequest;
import com.gdghongik.springsecurity.domain.member.service.MemberService;
import com.gdghongik.springsecurity.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/crud/members")
public class CRUDMemberController {

    private final MemberService memberService;

    // 내 정보 보기
    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        return ResponseEntity.ok(memberService.getMyInfo(memberId));
    }

    @PostMapping
    public ResponseEntity<Void> createMember(@RequestBody MemberCreateRequest request) {
        memberService.createMember(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<MemberInfoResponse>> getMembers() {
        return ResponseEntity.ok(memberService.getMembers());
    }

    @GetMapping
    public ResponseEntity<MemberInfoResponse> getMemberByUsername(@RequestParam String username) {
        return ResponseEntity.ok(memberService.getMemberByUsername(username));
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<Void> updateMember(@PathVariable Long memberId, @RequestBody MemberUpdateRequest request) {
        memberService.updateMember(memberId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok().build();
    }
}
