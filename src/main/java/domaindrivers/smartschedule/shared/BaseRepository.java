package domaindrivers.smartschedule.shared;

import java.util.List;
import java.util.Optional;


//to use if we don't want to expose findAll() method from spring-data-jpa for entities with possible large number of records
public interface BaseRepository<T, ID> {
        //extends Repository<T, ID>  {

    Optional<T> findById(ID id);

    T getReferenceById(ID id);

    boolean existsById(ID id);

    void deleteById(ID id);

    void delete(T entity);

    long count();

    void save(T entity);

    List<T> findAllById(List<ID> ids);

    void saveAll(List<T> entities);
}