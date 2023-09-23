package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * yml파일에서 profile에 설정한 값이 local test환경과
 * local에서 tomcat을 띄운 환경에서 차이를 주기 위함
 */
@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    /**
     * @PostConstruct와 @Transactional을 분리해주어야 한다. 동시에 사용못함
     */
    @PostConstruct
    public void init(){
        initMemberService.init();
    }
    @Component
    static class InitMemberService{
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init(){
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamA");
            em.persist(teamA);
            em.persist(teamB);

            for(int i = 0; i<100; i++){
               Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member"+i,i,selectedTeam));
            }
        }
    }
}
