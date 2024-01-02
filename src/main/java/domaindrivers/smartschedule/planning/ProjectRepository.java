package domaindrivers.smartschedule.planning;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProjectRepository extends JpaRepository<Project, ProjectId> {
}