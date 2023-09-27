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

        /*
            2. 검색 조건 쿼리

            실행된 JPQL

            select item
            from Item item
            where item.name = ?1 and item.price > ?2

            예)
            item.price.between(1000, 2000) // 가격이 1000~ 2000원인 상품
            item.name.contains("상품1")   // like '%상품1%'
            item.name.startsWith("고급")  // like '고급%'
         */
        JPAQuery query2 = new JPAQuery(em);
        QItem item = QItem.item;
        List<Item> list = query.from(item)
                .where(item.name.eq("좋은상품").and(item.price.gt(2000)))
                .list(item); // 조회할 프로젝션 지정

        /*
            3. 결과 조회

            쿼리 작성이 끝나고 결과 조회 메소드를 호출하면 실제 데이터베이스를 조회한다.
            보통 uniqueResult()나 list()를 사용하고 파라미터로 프로젝션 대상을 넘겨준다.

            - uniqueResult() : 조회 결과가 한 건일때 사용한다. 조회 결과가 없으면 null 반환
                하나 이상이면 예외가 발생한다.
            - singleResult() : uniqueResult()와 같지만 결과가 하나 이상이면 처음 데이터를 반환
            - list() : 결과가 하나 이상일 때 사용한다. 결과가 없으면 빈 컬렉션 반환
         */
    }
}
