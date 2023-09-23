package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(eqUsername(condition.getUsername()),
                        eqTeamName(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        /**
         * fetchResult()는 count 쿼리와 content 쿼리를 두번 날린다.
         * QueryResults<>로 받아야 한다.
         */
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(eqUsername(condition.getUsername()),
                        eqTeamName(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);

    }

    /**
     * content용과 count용 쿼리를 따로 만든다.
     * 언제 사용하느냐. count를 할때 join을 하지 않거나, 줄여서 할 수 있을때
     * fetchCount는 groupby와 같이 쓸때 생기는 문제로인해, fetch().size() 대용
     * fetchResults도 groupby랑 쓰면 문제 생김
     * 최적화 가능
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        /**
         * content용 쿼리
         */
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(eqUsername(condition.getUsername()),
                        eqTeamName(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        /**
         * count용 쿼리
         */
        //JPAQuery<Member> countQuery = queryFactory
        long total = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        eqUsername(condition.getUsername()),
                        eqTeamName(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch().size();


        return new PageImpl<>(content, pageable, total);
        /**
         *   page의 시작이면서 content size가 page size보다 작거나,
         * 페이지의 마지막이라면, offset+content size만으로 totalcount가 되므로
         * total count를 호출할 필요가 없다. 아래 코드가 그런 역할을 함
         *
         /**
         * return PageableExecutionUtils.getPage(content,pageable,()-> countQuery.fetchCount());
         *
         */

    }

    private BooleanExpression eqUsername(String usernameCond) {
        return hasText(usernameCond) ? member.username.containsIgnoreCase(usernameCond) : null;
    }

    private BooleanExpression eqTeamName(String teamNameCond) {
        return hasText(teamNameCond) ? team.name.containsIgnoreCase(teamNameCond) : null;
    }

    private BooleanExpression ageGoe(Integer ageCond) {
        return ageCond != null ? member.age.goe(ageCond) : null;
    }

    private BooleanExpression ageLoe(Integer ageCond) {
        return ageCond != null ? member.age.loe(ageCond) : null;
    }


}
