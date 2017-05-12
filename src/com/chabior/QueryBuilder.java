package com.chabior;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;

class QueryBuilder {

    private String source;

    QueryBuilder(String source) {
        this.source = source;
    }

    String build() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse(this.source);
        PlainSelect select = (PlainSelect) ((Select) stmt).getSelectBody();

        StringBuilder queryBuilder = new StringBuilder("$qb\n->select([\n");

        for (SelectItem item : select.getSelectItems()) {
            queryBuilder.append("'" + item.toString() + "',\n");
        }
        queryBuilder.append("])\n");
        queryBuilder.append("->from('");
        Table table = (Table) select.getFromItem();
        queryBuilder.append(table.getName());
        queryBuilder.append("', '");
        queryBuilder.append(table.getAlias().getName() + "')\n");

        List<Join> joins = select.getJoins();
        for (Join join : joins) {
            if (join.isLeft()) {
                queryBuilder.append("->leftJoin(");
            } else if (join.isInner()) {
                queryBuilder.append("->innerJoin(");
            } else {
                queryBuilder.append("->innerJoin(");
            }

            Table joinTable = (Table) join.getRightItem();
            queryBuilder.append("'" + this.getJoinSource(joinTable, (BinaryExpression) join.getOnExpression()) + "', ");

            queryBuilder.append("'" + joinTable.getName() + "', ");
            queryBuilder.append("'" + joinTable.getAlias().getName() + "'");

            if (join.getOnExpression() != null) {
                queryBuilder.append(", '" + join.getOnExpression() + "'");
            }

            queryBuilder.append(")\n");
        }

        if (select.getWhere() != null) {
            queryBuilder.append("\n->where('" + select.getWhere().toString() + "')");

        }

        if (select.getGroupByColumnReferences() != null) {
            for (Expression expression : select.getGroupByColumnReferences()) {
                queryBuilder.append("\n->addGroupBy('" + expression.toString() + "')");
            }
        }

        if (select.getOrderByElements() != null) {
            for (OrderByElement orderByElement : select.getOrderByElements()) {
                Expression expression = orderByElement.getExpression();
                String orderBy;
                if (orderByElement.isAsc()) {
                    orderBy = "ASC";
                } else {
                    orderBy = "DESC";
                }
                queryBuilder.append("\n->addOrderBy('" + expression + "', '" + orderBy + "')");
            }
        }

        if (select.getHaving() != null) {
            queryBuilder.append("\n->having('" + select.getHaving().toString() + "')");
        }

        if (select.getLimit() != null) {
            queryBuilder.append("\n->setMaxResults(" + select.getLimit().getRowCount().toString() +")");
        }

        if (select.getOffset() != null) {
            queryBuilder.append("\n->setFirstResult(" + select.getOffset().getOffset() + ")");
        }

        queryBuilder.append("\n\n;");

        return queryBuilder.toString();
    }

    private String getJoinSource(Table joinTable, BinaryExpression expression) {
        String alias = joinTable.getAlias().getName();
        Table leftTable;
        Table rightTable;
        if (expression.getLeftExpression() instanceof Column) {
            leftTable = ((Column) (expression.getLeftExpression())).getTable();
            rightTable = ((Column) (expression.getRightExpression())).getTable();
        } else {
            EqualsTo equalsTo = (EqualsTo) expression.getLeftExpression();
            leftTable = ((Column) (equalsTo.getLeftExpression())).getTable();
            rightTable = ((Column) equalsTo.getRightExpression()).getTable();
        }

        if (leftTable.getName() != null && !leftTable.getName().equals(alias)) {
            return leftTable.getName();
        }

        if (rightTable.getName() != null && !rightTable.getName().equals(alias)) {
            return rightTable.getName();
        }

        return "REPLACE";
    }
}

