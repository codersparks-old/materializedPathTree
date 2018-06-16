package uk.codersparks.communitytreematerializedpath.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
public class Community {

    public static final String PATH_SEPERATOR = "/";

    @Id
    private String name;

    private String path = "";}
