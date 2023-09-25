import com.mysema.query.jpa.impl.JPAQuery;
import entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class Main {

    /**
     * QueryDSL 시작
     */
    public void queryDSL() {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa_lab_8_2");
        EntityManager em = emf.createEntityManager();

        // QueryDSL을 사용하려면 JPAQuery 객체 생성
        JPAQuery query = new JPAQuery(em);
        // 사용할 쿼리 타입(Q)을 생성, 생성자에는 별칭을 준다
        // 이 별칭을 JPQL에서 별칭으로 사용한다.
        QMember qMember = new QMember("m");
        List<Member> members =
                query.from(qMember)
                        .where(qMember.name.eq("회원"))
                        .orderBy(qMember.name.desc())
                        .list(qMember);
    }
}
