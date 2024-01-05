package domaindrivers.smartschedule.planning;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


class RedisProjectRepository implements ProjectRepository {

    private final RedisTemplate<String, Project> redisTemplate;

    RedisProjectRepository(RedisTemplate<String, Project> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<Project> findById(ProjectId projectId) {
        return Optional.ofNullable(redisTemplate.<String, Project>opsForHash().get("projects", projectId.id().toString()));
    }

    @Override
    public Project save(Project project) {
        redisTemplate.opsForHash().put("projects", project.id().id().toString(), project);
        return project;
    }

    @Override
    public List<Project> findAllByIdIn(Set<ProjectId> projectId) {
        Set<String> ids = projectId.stream().map(p -> p.id().toString()).collect(Collectors.toSet());
        return redisTemplate.<String, Project>opsForHash().multiGet("projects", ids);
    }

    @Override
    public List<Project> findAll() {
        return redisTemplate.<String, Project>opsForHash().values("projects").stream().collect(Collectors.toList());
    }

}