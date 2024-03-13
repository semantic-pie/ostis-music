package io.github.semanticpie.derezhor.externalAgents.users.services.impl;

import io.github.semanticpie.derezhor.externalAgents.users.models.ScUser;
import io.github.semanticpie.derezhor.externalAgents.users.models.enums.UserRole;
import io.github.semanticpie.derezhor.externalAgents.users.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ostis.api.context.DefaultScContext;
import org.ostis.scmemory.model.element.ScElement;
import org.ostis.scmemory.model.element.edge.EdgeType;
import org.ostis.scmemory.model.element.edge.ScEdge;
import org.ostis.scmemory.model.element.link.LinkType;
import org.ostis.scmemory.model.element.link.ScLinkString;
import org.ostis.scmemory.model.element.node.NodeType;
import org.ostis.scmemory.model.element.node.ScNode;
import org.ostis.scmemory.model.exception.ScMemoryException;
import org.ostis.scmemory.model.pattern.ScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.DefaultWebsocketScPattern;
import org.ostis.scmemory.websocketmemory.memory.pattern.SearchingPatternTriple;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.AliasPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.FixedPatternElement;
import org.ostis.scmemory.websocketmemory.memory.pattern.element.TypePatternElement;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private DefaultScContext context;

    @Override
    public Optional<ScUser> createUser(final String username,
                                       final String password,
                                       final UserRole userRole) {

        try {
            UUID uuid = UUID.randomUUID();
            var strUUID = uuid.toString().replace('-', '_');

            // concept_user -> userStruct
            ScNode conceptUser = context.resolveKeynode("concept_user", NodeType.CONST_CLASS);
            ScNode userStruct = context.resolveKeynode(strUUID, NodeType.CONST_STRUCT);
            context.createEdge(EdgeType.ACCESS_CONST_POS_PERM, conceptUser, userStruct);

            // userStruct => userRoleName
            // nrel_user_role -> edge
            ScNode nrelUserRole = context.resolveKeynode("nrel_user_role", NodeType.CONST_NO_ROLE);
            ScLinkString scLinkUserRole = context.createStringLink(LinkType.LINK_CONST, userRole.name());
            ScEdge userRoleEdge = context.createEdge(EdgeType.D_COMMON_CONST, userStruct, scLinkUserRole);
            context.createEdge(EdgeType.ACCESS_CONST_POS_PERM, nrelUserRole, userRoleEdge);

            // lang_en -> username
            ScLinkString scLinkUsername = context.createStringLink(LinkType.LINK_CONST, username);
            ScNode langEn = context.findKeynode("lang_en").orElseThrow();
            context.createEdge(EdgeType.ACCESS_CONST_POS_PERM, langEn, scLinkUsername);

            // userStruct => username + nrel_username
            ScNode noRoleUsername = context.resolveKeynode("nrel_username", NodeType.CONST_NO_ROLE);
            ScEdge userStructUsernameEdge = context.createEdge(EdgeType.D_COMMON_CONST, userStruct, scLinkUsername);
            context.createEdge(EdgeType.ACCESS_CONST_POS_PERM, noRoleUsername, userStructUsernameEdge);

            savePassword(userStruct, password);

            log.info("Created user: [{}:{}:{}]", strUUID, username, password);
            return Optional.of(ScUser.builder()
                    .uuid(strUUID)
                    .username(username)
                    .password(password)
                    .userRole(userRole)
                    .build());
        } catch (Exception ex) {
            log.error("Can't create user", ex);
            return Optional.empty();
        }
    }

    private void savePassword(ScNode userStruct, String rawPassword) throws ScMemoryException {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(rawPassword);

        ScNode noRolePassword = context.resolveKeynode("nrel_password", NodeType.CONST_NO_ROLE);

        // userStruct => scLinkHashPassword + nrel_password
        ScLinkString scLinkHashPassword = context.createStringLink(LinkType.LINK_CONST, hashedPassword);
        ScEdge userStructHashPassEdge = context.createEdge(EdgeType.D_COMMON_CONST, userStruct, scLinkHashPassword);
        context.createEdge(EdgeType.ACCESS_CONST_POS_PERM, noRolePassword, userStructHashPassEdge);
    }

    @Override
    public Optional<String> getUserUUID(String username) {
        try {
            Optional<ScLinkString> userScLinkString = getUsernameScLink(username);

            if (userScLinkString.isEmpty()) {
                return Optional.empty();
            }

            ScPattern p = new DefaultWebsocketScPattern();
            var conceptUser = context.findKeynode("concept_user").orElseThrow();
            var sysIdtf = context.findKeynode("nrel_system_identifier").orElseThrow();

            p.addElement(new SearchingPatternTriple(
                    new FixedPatternElement(conceptUser),
                    new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_1")),
                    new TypePatternElement<>(NodeType.VAR, new AliasPatternElement("var_user_struct"))
            ));

            p.addElement(new SearchingPatternTriple(
                    new AliasPatternElement("var_user_struct"),
                    new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("var_user_struct==>username")),
                    new FixedPatternElement(userScLinkString.get())
            ));

            p.addElement(new SearchingPatternTriple(
                    new AliasPatternElement("var_user_struct"),
                    new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("var_user_struct==>uuid_link")),
                    new TypePatternElement<>(LinkType.LINK_VAR, new AliasPatternElement("uuid_link"))
            ));

            p.addElement(new SearchingPatternTriple(
                    new FixedPatternElement(sysIdtf),
                    new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_2")),
                    new AliasPatternElement("var_user_struct==>uuid_link")
            ));

            Stream<Stream<? extends ScElement>> result = context.find(p);

            return result
                    .flatMap(innerStream -> innerStream.filter(link -> link instanceof ScLinkString)
                            .map(link -> (ScLinkString) link))
                    .filter(link -> !link.getContent().equals(username))
                    .findFirst()
                    .map(ScLinkString::getContent);

        } catch (ScMemoryException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ScLinkString> getUsernameScLink(String username) {
        try {
            var nrelUsername = context.findKeynode("nrel_username").orElseThrow();
            ScPattern p = new DefaultWebsocketScPattern();

            // see edge alias
            p.addElement(new SearchingPatternTriple(
                    new TypePatternElement<>(NodeType.VAR, new AliasPatternElement("var_user_struct")),
                    new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("var_user_struct==>username")),
                    new TypePatternElement<>(LinkType.LINK_VAR, new AliasPatternElement("username"))
            ));

            // link nrel_username with previous edge
            p.addElement(new SearchingPatternTriple(
                    new FixedPatternElement(nrelUsername),
                    new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("nrel_username-->(track=>hash)")),
                    new AliasPatternElement("var_user_struct==>username")
            ));

            Stream<Stream<? extends ScElement>> result = context.find(p);

            return result
                    .flatMap(innerStream -> innerStream.filter(link -> link instanceof ScLinkString)
                            .map(link -> (ScLinkString) link)
                            .filter(link -> link.getContent().equals(username)))
                    .findFirst();

        } catch (ScMemoryException ex) {
            return Optional.empty();
        }
    }

    // добаить информативности чего у юзера нет
    @Override
    public Optional<ScUser> findByUsername(String username) {
        try {
            var uuid = getUserUUID(username).orElseThrow();
            var password = getUserPassword(uuid).orElseThrow();
            var userRole = getUserRole(uuid).orElseThrow();


            return Optional.of(ScUser.builder()
                    .uuid(uuid)
                    .username(username)
                    .password(password)
                    .userRole(userRole)
                    .build());
        } catch (NoSuchElementException ex) {
            log.info("User with [{}] not found", username);
            return Optional.empty();
        }

    }

    private Optional<String> getUserPassword(String uuid) {
        return getScLinkStringContent(uuid, "nrel_password");
    }

    private Optional<UserRole> getUserRole(String uuid) {
        var optUserRoleContent = getScLinkStringContent(uuid, "nrel_user_role");

        if (optUserRoleContent.isPresent()) {
            try {
                return Optional.of(UserRole.valueOf(optUserRoleContent.get()));
            } catch (IllegalArgumentException e) {
                log.error("Error of casting content UserRoleLink to UserRole");
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * This method search construction like sourceNode ==> scLinkString with nrelRelation
     * and returns the content of scLinkString.
     * @param sourceNodeIdtf represents source node idtf from arrow go
     * @param nrelRelationWithScLinkIdtf represents relation how node and link connected
     * @return content of found scLinkString
     */
    public Optional<String> getScLinkStringContent(String sourceNodeIdtf, String nrelRelationWithScLinkIdtf) {
        try {
            var sourceNode = context
                    .findKeynode(sourceNodeIdtf)
                    .orElseThrow();
            var nrelRelation = context
                    .findKeynode(nrelRelationWithScLinkIdtf)
                    .orElseThrow();
            ScPattern p = new DefaultWebsocketScPattern();

            p.addElement(new SearchingPatternTriple(
                    new FixedPatternElement(sourceNode),
                    new TypePatternElement<>(EdgeType.D_COMMON_VAR, new AliasPatternElement("sourceNode=>scLinkString")),
                    new TypePatternElement<>(LinkType.LINK_VAR, new AliasPatternElement("scLinkString"))
            ));

            p.addElement(new SearchingPatternTriple(
                    new FixedPatternElement(nrelRelation),
                    new TypePatternElement<>(EdgeType.ACCESS_VAR_POS_PERM, new AliasPatternElement("edge_1")),
                    new AliasPatternElement("sourceNode=>scLinkString")
            ));

            Stream<Stream<? extends ScElement>> result = context.find(p);

            return result
                    .flatMap(innerStream -> innerStream.filter(link -> link instanceof ScLinkString)
                            .map(link -> (ScLinkString) link))
                    .findFirst()
                    .map(ScLinkString::getContent);


        } catch (ScMemoryException e) {
            log.info("Can't find link between {} and {}", sourceNodeIdtf, nrelRelationWithScLinkIdtf);
            return Optional.empty();
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ScUser user = findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("User '%s' not found", username)
        ));

        List<GrantedAuthority> authorities = new ArrayList<>(Collections.emptyList());
        authorities.add((GrantedAuthority) () -> user.getUserRole().name());

        // Convert our User to which Spring Security understand
        return new User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
