package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.shared.EventsPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Import({TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = {"classpath:schema-planning.sql"})
class PlanningFacadeIntegrationTest {

    @Autowired
    PlanningFacade projectFacade;

    @Autowired
    EventsPublisher eventsPublisher;

    @Test
    void canCreateProjectAndLoadProjectCard() {
        //given
        ProjectId projectId =
                projectFacade.addNewProject("project", new Stage("Stage1"));

        //when
        ProjectCard loaded = projectFacade.load(projectId);

        //then
        assertEquals(projectId, loaded.projectId());
        assertEquals("project", loaded.name());
        assertEquals("Stage1", loaded.parallelizedStages().print());
    }

}