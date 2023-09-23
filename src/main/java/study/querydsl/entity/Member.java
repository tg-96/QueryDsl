package study.querydsl.entity;

import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of ={"id","username","age"})
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private long id;

    private String username;
    private int age;
    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username, int age, Team team) {
        this.username = username;
         this.age = age;
        if(team != null){
            changeTeam(team);
        }
    }

    public Member(String username) {
        this(username,0);
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }


    private void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
