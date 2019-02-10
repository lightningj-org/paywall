package org.lightningj.paywall.springboot2


import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class DemoApplicationSpec extends Specification{

    def "contextLoads"(){
        when:
        true
        then:
        true
    }
}
