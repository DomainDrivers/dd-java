package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.planning.parallelization.ParallelStages;
import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static java.time.Instant.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes = {RedisConfiguration.class})
class RedisRepositoryTest {

    static final TimeSlot JAN_10_20 = new TimeSlot(parse("2020-01-10T00:00:00.00Z"), parse("2020-01-20T00:00:00.00Z"));
    static final ChosenResources NEEDED_RESOURCES = new ChosenResources(Set.of(ResourceId.newOne()), JAN_10_20);
    static final Schedule SCHEDULE = new Schedule(Map.of("Stage1", JAN_10_20));
    static final Demands DEMAND_FOR_JAVA = new Demands(List.of(new Demand(skill("JAVA"))));
    static final DemandsPerStage DEMANDS_PER_STAGE = DemandsPerStage.empty();
    static final ParallelStagesList STAGES = ParallelStagesList.of(new ParallelStages(Set.of(new Stage("Stage1"))));

    static {
        GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);
        redis.start();
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getFirstMappedPort() + "");
    }

    @Autowired
    ProjectRepository redisProjectRepository;

    @Test
    void canSaveAndLoadProject() {
        //given
        Project project = new Project("project", STAGES);
        //and
        project.addSchedule(SCHEDULE);
        //and
        project.addDemands(DEMAND_FOR_JAVA);
        //and
        project.addChosenResources(NEEDED_RESOURCES);
        //and
        project.addDemandsPerStage(DEMANDS_PER_STAGE);
        //and
        project = redisProjectRepository.save(project);

        //when
        Optional<Project> loaded =
                redisProjectRepository.findById(project.id());

        //then
        assertThat(loaded).isPresent();
        assertEquals(NEEDED_RESOURCES, loaded.get().getChosenResources());
        assertEquals(STAGES, loaded.get().getParallelizedStages());
        assertEquals(SCHEDULE, loaded.get().getSchedule());
        assertEquals(DEMAND_FOR_JAVA, loaded.get().getAllDemands());
        assertEquals(DEMANDS_PER_STAGE, loaded.get().getDemandsPerStage());
    }

    @Test
    void canLoadMultipleProjects() {
        //given
        Project project = new Project("project", STAGES);
        Project project2 = new Project("project2", STAGES);

        //and
        project = redisProjectRepository.save(project);
        project2 = redisProjectRepository.save(project2);

        //when
        List<Project> loaded =
                redisProjectRepository.findAllByIdIn(Set.of(project.id(), project2.id()));

        //then
        assertThat(loaded).hasSize(2);
        Set<ProjectId> ids = loaded.stream().map(Project::id).collect(Collectors.toSet());
        assertThat(ids).containsExactlyInAnyOrder(project2.id(), project.id());
    }

    @Test
    void canLoadAllProjects() {
        //given
        Project project = new Project("project", STAGES);
        Project project2 = new Project("project2", STAGES);

        //and
        project = redisProjectRepository.save(project);
        project2 = redisProjectRepository.save(project2);

        //when
        List<Project> loaded =
                redisProjectRepository.findAll();

        //then
        assertThat(loaded).hasSize(2);
        Set<ProjectId> ids = loaded.stream().map(Project::id).collect(Collectors.toSet());
        assertThat(ids).containsExactlyInAnyOrder(project2.id(), project.id());
    }

}