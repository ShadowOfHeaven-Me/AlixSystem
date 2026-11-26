package ua.nanit.limbo.util.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TesterDomain {

    @Test
    public void testDomain() throws Exception {
        var h = DomainUtils.extractHost0("_dc-srv.357cb6b5e83a._minecraft._tcp.pixelfox.pl.");
        //Assertions.assertNotEquals(DomainUtils.INVALID_HOST, h);
        System.out.println(h);
    }
}
