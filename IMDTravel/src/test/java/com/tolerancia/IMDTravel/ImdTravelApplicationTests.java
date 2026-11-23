package com.tolerancia.IMDTravel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "imdtravel.sales.file=${java.io.tmpdir}/imdtravel-test-sales.jsonl")
class ImdTravelApplicationTests {

	@Test
	void contextLoads() {
	}

}
