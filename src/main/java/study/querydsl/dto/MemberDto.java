package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;

    /**
     * @QueryProjection Dto도 Qfile로 생성해준다. gradle->other->compilequerydsl 실행
     * querydsl에 대한 의존성이 생겨버림
     */
    @QueryProjection
    public MemberDto(String username, int age){
        this.username = username;
        this.age = age;
    }





}
