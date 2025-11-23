package org.in.media.res.sqlBuilder.api.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SqlFormatterPrettyPrintTest {

	@Test
	void prettyPrintFormatsCteAndJoinsWithIndentedPredicates() {
		String sql = """
				WITH salary_avg AS (SELECT e.id, AVG(j.salary) AS avg_salary FROM employee e LEFT JOIN job j ON e.id = j.emp_id WHERE j.salary > ? GROUP BY e.id)
				SELECT e.first_name, sa.avg_salary FROM employee e JOIN salary_avg sa ON e.id = sa.id WHERE e.state IN ('CA','OR') AND (sa.avg_salary > 100000 OR e.title LIKE 'Lead%') ORDER BY sa.avg_salary DESC, e.last_name ASC
				""";

		String pretty = SqlFormatter.prettyPrint(sql);

		String expected = """
				WITH
				  salary_avg AS (
				    SELECT
				      e.id,
				      AVG(j.salary) AS avg_salary
				    FROM
				      employee e
				      LEFT JOIN job j
				        ON e.id = j.emp_id
				    WHERE
				      j.salary > ?
				    GROUP BY
				      e.id
				  )
				SELECT
				  e.first_name,
				  sa.avg_salary
				FROM
				  employee e
				  JOIN salary_avg sa
				    ON e.id = sa.id
				WHERE
				  e.state IN ('CA','OR')
				  AND (
				    sa.avg_salary > 100000
				    OR e.title LIKE 'Lead%'
				  )
				ORDER BY
				  sa.avg_salary DESC,
				  e.last_name ASC""";

		assertEquals(expected, pretty);
	}

	@Test
	void prettyPrintIndentedJoinWithDerivedTableAlias() {
		String sql = """
				SELECT "E"."FIRST_NAME" as "firstName", "E"."LAST_NAME" as "lastName" FROM "Employee" "E" JOIN (SELECT "E"."ID", AVG("J"."SALARY") FROM "Employee" "E" JOIN "Job" "J" ON "E"."ID" = "J"."EMPLOYEE_ID" GROUP BY "E"."ID") "SALARY_AVG" ON "E"."ID" = "SALARY_AVG"."EMPLOYEE_ID" WHERE ("SALARY_AVG"."AVG_SALARY" >= ?) AND ("E"."LAST_NAME" LIKE ? ESCAPE '\\\\' OR "E"."MAIL" LIKE ? ESCAPE '\\\\') ORDER BY "E"."LAST_NAME" ASC
				""";

		String pretty = SqlFormatter.prettyPrint(sql);

		String expected = """
				SELECT
				  "E"."FIRST_NAME" as "firstName",
				  "E"."LAST_NAME" as "lastName"
				FROM
				  "Employee" "E"
				  JOIN (
				    SELECT
				      "E"."ID",
				      AVG("J"."SALARY")
				    FROM
				      "Employee" "E"
				      JOIN "Job" "J"
				        ON "E"."ID" = "J"."EMPLOYEE_ID"
				    GROUP BY
				      "E"."ID"
				  ) "SALARY_AVG"
				    ON "E"."ID" = "SALARY_AVG"."EMPLOYEE_ID"
				WHERE
				  (
				    "SALARY_AVG"."AVG_SALARY" >= ?
				  )
				  AND (
				    "E"."LAST_NAME" LIKE ? ESCAPE '\\\\'
				    OR "E"."MAIL" LIKE ? ESCAPE '\\\\'
				  )
				ORDER BY
				  "E"."LAST_NAME" ASC""";

		assertEquals(expected, pretty);
	}
}
