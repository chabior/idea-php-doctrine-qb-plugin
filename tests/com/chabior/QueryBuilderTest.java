package com.chabior;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class QueryBuilderTest {
    @Test
    public void build() throws Exception {
        QueryBuilder queryBuilder = new QueryBuilder();
        String query = queryBuilder.build("select u.id, u.username AS test FROM user u JOIN test t on t.user_id = u.id JOIN aba a on a.id = u.id WHERE a.id = 12 AND b.id = 14 GROUP BY a.id ORDER BY u.test DESC LIMIT 10 OFFSET 0;");

        assertEquals("$qb\n->select([\n'u.id',\n'u.username AS test',\n])\n->from('user', 'u')\n->innerJoin('u', 'test', 't', 't.user_id = u.id')\n->innerJoin('u', 'aba', 'a', 'a.id = u.id')\n\n->where('a.id = 12 AND b.id = 14')\n->addGroupBy('a.id')\n->addOrderBy('u.test', 'DESC')\n->setMaxResults(10)\n->setFirstResult(0);", query);
    }

}