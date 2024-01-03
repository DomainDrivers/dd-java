package domaindrivers.smartschedule.resource.device;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.capability.Capability;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static domaindrivers.smartschedule.shared.capability.Capability.assets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-resources.sql"})
class CreatingDeviceTest {

    @Autowired
    DeviceFacade deviceFacade;

    @Test
    void canCreateAndLoadDevices() {
        //given
        DeviceId device = deviceFacade.createDevice("super-excavator-1000", assets("BULLDOZER", "EXCAVATOR"));

        //when
        DeviceSummary loaded = deviceFacade.findDevice(device);

        //then
        assertThat(loaded.assets()).containsExactlyElementsOf(assets("BULLDOZER", "EXCAVATOR"));
        assertEquals("super-excavator-1000", loaded.model());
    }

    @Test
    void canFindAllCapabilities() {
        //given
        deviceFacade.createDevice("super-excavator-1000", assets("SMALL-EXCAVATOR", "BULLDOZER"));
        deviceFacade.createDevice("super-excavator-2000", assets("MEDIUM-EXCAVATOR", "UBER-BULLDOZER"));
        deviceFacade.createDevice("super-excavator-3000", assets("BIG-EXCAVATOR"));

        //when
        List<Capability> loaded = deviceFacade.findAllCapabilities();

        //then
        assertThat(loaded).contains(
                Capability.asset("SMALL-EXCAVATOR"),
                Capability.asset("BULLDOZER"),
                Capability.asset("MEDIUM-EXCAVATOR"),
                Capability.asset("UBER-BULLDOZER"),
                Capability.asset("BIG-EXCAVATOR")
        );
    }
}