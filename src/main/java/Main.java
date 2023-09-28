import com.mysema.query.SearchResults;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.SQLOutput;
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

        /*
            4. 페이징과 정렬

             정렬은 orderBy를 사용하는데 쿼리 타입(Q)이 제공하는 asc(), desc()를 사용한다.
             페이징은 offset과 limit를 적절히 조합해서 사용하면 된다.

         */
        QItem item = QItem.item;

        query.from(item)
                .where(item.price.gt(2000))
                .orderBy(item.price.desc(), item.stockQuantity.asc())
                .offset(10).limit(20)
                .list(item);

        // 실제 페이징 처리를 하려면 검색된 전체 데이터 수를 알아야 한다.
        // 이 때는 list() 대신에 listResults()를 사용한다.
        SearchResults<Item> result =
                query.from(item)
                        .where(item.price.get(10000))
                        .offset(10).limit(20)
                        .listResults(item);

        long total = result.getTotal(); // 검색된 전체 데이터 수
        long limit = result.getLimit();
        long offset = result.getOffset();
        List<Item> results = result.getResults();

        /*
            5. 그룹

            그룹화는 groupBy를 사용하고 결과를 제한하려면 having을 사용하면 된다.
         */
        query.from(item)
                .groupBy(item.price)
                .having(item.price.gt(1000))
                .list(item);

        /*
            6. 조인

             조인은 innerJoin, leftJoin, rightJoin, fullJoin을 사용할 수 있고
            JPQL의 on과 성능 최적화를 위한 fetch 조인도 사용할 수 있다.

             조인의 기본 문법은 첫 번째 파라미터에 조인 대상을 지정하고,
            두 번째 파라미터에 별칭으로 사용할 쿼리 타입을 지정하면 된다.

            join(조인 대상, 별칭으로 사용할 쿼리 타입)
         */
        // 기본적인 조인 방법
        QOrder order = QOrder.order;
        QMember member = QMember.member;
        QOrderItem orderItem = QOrderItem.orderItem;

        query.from(order)
                .join(order.member, member)
                .leftJoin(order.orderItems, orderItem)
                .list(order);

        // 조인 on 사용
        query.from(order)
                .leftJoin(order.orderItem, orderItem)
                .on(orderItem.count.gt(2))
                .list(order);
        // 페치 조인
        query.from(order)
                .innerJoin(order.member, member).fetch()
                .leftJoin(order.orderItems, orderItem).fetch()
                .list(order);
        // from절에 여러 조인을 사용하는 세타 조인
        query.from(order, member)
                .where(order.member.eq(member))
                .list(order);

        /*
            7. 서브 쿼리

             서브 쿼리는 com.mysema.query.jpa.JPASubQuery를 생성해서 사용한다.
            서브 쿼리의 결과가 하나면 unique(), 여러 건이면 list()를 사용한다.
         */

        // 한 건
        QItem item = QItem.item;
        QItem itemSub = new QItem("itemSub");

        query.from(item)
                .where(item.price.eq(
                        new JPASubQuery().from(itemSub).unique(itemSub.price.max())
                ))
                .list(item);

        // 여러 건
        QItem item = QItem.item;
        QItem itemSub = new QItem("itemSub");

        query.from(item)
                .where(item.in(
                        new JPASubQuery().from(itemSub)
                                .where(item.name.eq(itemSub.name))
                                .list(itemSub)
                ))
                .list(item);

        /*
            8. 프로젝션과 결과 반환

         */
        // 프로젝션 대상이 하나
        // item.name <- 해당 타입으로 반환한다.
        QItem item = QItem.item;
        List<String> result = query.from(item).list(item.name);

        for (String name : result) {
            System.out.println("name = " + name);
        }
        // 여러 컬럼 반환과 튜플
        // 프로젝션 대상으로 여러 필드를 선택하면 QueryDSL은 기본으로 com.mysema.query.Tuple이라는
        // Map과 비슷한 내부 타입을 사용한다.
        // 조회 결과는 tuple.get() 메소드에 조회한 쿼리 타입을 지정하면 된다.
        QItem item = QItem.item;

        List<Tuple> result = query.from(item).list(item.name, item.price);
        // List<Tuple> result = query.from(item).list(new QTuple(item.name, item.price)); 와 같다

        for (Tuple tuple : result) {
            System.out.println("name = " + tuple.get(item.name));
            System.out.println("name = " + tuple.get(item.price));
        }
    }
}
