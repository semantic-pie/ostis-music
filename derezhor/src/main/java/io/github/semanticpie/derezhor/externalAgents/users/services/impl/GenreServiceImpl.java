package io.github.semanticpie.derezhor.externalAgents.users.services.impl;


import io.github.semanticpie.derezhor.common.services.CacheService;
import io.github.semanticpie.derezhor.externalAgents.users.dtos.GenreDTO;
import io.github.semanticpie.derezhor.externalAgents.users.services.GenreService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.edge.ScEdge;
import org.ostis.scmemory.model.element.link.LinkType;
import org.ostis.scmemory.model.element.link.ScLink;
import org.ostis.scmemory.model.element.node.ScNode;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class GenreServiceImpl implements GenreService {

    private DefaultScContext context;

    private CacheService service;

    @Override
    public void addGenres(String userUUID, List<GenreDTO> genreDTOList) {

        ScNode genreNode = service.get("user_genres");
        ScNode weightRelation = service.get("nrel_weight");
        List<ScElement> elements = new ArrayList<>(List.of(genreNode, weightRelation));

        try {
            ScNode user = context.findKeynode(userUUID).orElseThrow(ScMemoryException::new);
            for (GenreDTO genre : genreDTOList) {
                ScNode genreConcept = service.get(genre.getName());

                ScLink weightNode = context.createStringLink(LinkType.LINK_CONST, Integer.toString(genre.getWeight()));
                ScEdge weightRelationEdge = context.resolveEdge(genreConcept, EdgeType.D_COMMON_VAR, weightNode);

                ScEdge genreNodeGenreConceptEdge = context.resolveEdge(genreNode, EdgeType.ACCESS_VAR_POS_PERM, genreConcept);

                ScEdge weightRelationWeightRelationEdge = context.resolveEdge(weightRelation, EdgeType.ACCESS_VAR_POS_PERM, weightRelationEdge);

                context.resolveEdge(user, EdgeType.ACCESS_VAR_POS_PERM, weightRelationEdge);

                elements.addAll(List.of(
                        genreNodeGenreConceptEdge,
                        weightRelationWeightRelationEdge,
                        genreConcept, weightNode));
            }

            context.createEdges(
                    Stream.generate(() -> EdgeType.ACCESS_CONST_POS_PERM).limit(elements.size()),
                    Stream.generate(() -> user).limit(elements.size()),
                    elements.stream());
        } catch (ScMemoryException e) {
            log.error("Can't create user's genres", e);
        }
    }
}


