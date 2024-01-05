package domaindrivers.smartschedule.planning;


import org.springframework.data.jpa.repository.JpaRepository;


interface JpaProjectRepository extends ProjectRepository, JpaRepository<Project, ProjectId> {

}