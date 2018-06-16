package uk.codersparks.communitytreematerializedpath.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.codersparks.communitytreematerializedpath.model.Community;

import java.util.List;

public interface CommunityRepository extends MongoRepository<Community, String> {

    List<Community> findAllByOrderByPathAsc();
}
