package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
@Profile("test")
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest(){
        Member member = new Member("member1",10);
        memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findByUsername("member1");
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findAll();
        assertThat(result2).containsExactly(member);

    }

    @Test
    public void searchByParameter(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
//        em.persist(teamA);
//        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",25,teamA);
        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setAgeGoe(20);
        cond.setUsername("member");
        cond.setAgeLoe(30);
        cond.setTeamName("teamA");

        List<MemberTeamDto> findMembers = memberRepository.search(cond);

        assertThat(findMembers.get(0).getUsername()).isEqualTo("member2");
    }

    @Test
    public void simplePage(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
//        em.persist(teamA);
//        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);
        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        MemberSearchCondition cond = new MemberSearchCondition();

        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(cond,pageRequest);

        assertThat(result.getContent())
                .extracting("username")
                .containsExactly("member1","member2","member3");
        assertThat(result.getSize()).isEqualTo(3);
    }

    /**
     * spring data jpa에 파라미터 부분에 조건을 넣을 수 있다.
     * repository가 querydsl 의존적
     * 실무에서 쓰기는 쉽지 않다... left join 안됨
     *
     */
    @Test
    public void querydslPredicateExecutorTest(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
//        em.persist(teamA);
//        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);
        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        Iterable<Member> result = memberRepository.findAll(member.age.between(10,40).and(member.username.eq("member1")));
        for (Member findMember : result) {
            System.out.println("member1 = " + findMember);
        }
    }


}