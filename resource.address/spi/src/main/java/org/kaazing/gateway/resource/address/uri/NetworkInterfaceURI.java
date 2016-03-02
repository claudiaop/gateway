/**
 * Copyright 2007-2015, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaazing.gateway.resource.address.uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class performing logic similar to java.net.URI class which supports network interface syntax
 *
 */
public class NetworkInterfaceURI {

    private static final String HOST_TEMPLATE = "127.0.0.1";

    private URI mockNetworkInterfaceURI;

    // -- Properties and components of this instance -- similar to java.net.URI

    // Components of all URIs: [<scheme>:]<scheme-specific-part>[#<fragment>]
    private String scheme;            // null ==> relative URI
    private String fragment;

    // Hierarchical URI components: [//<authority>]<path>[?<query>]
    private String authority;         // Registry or server

    // Server-based authority: [<userInfo>@]<host>[:<port>]
    private String userInfo;
    private String host;              // null ==> registry-based
    private int port = -1;            // -1 ==> undefined

    // Remaining components of hierarchical URIs
    private String path;              // null ==> opaque
    private String query;

    private boolean absolute;

    public static String buildURIToString(String scheme, String authority, String path, String query, String fragment) {
        URI helperURI = null;
        try {
            helperURI = new URI(scheme, HOST_TEMPLATE, path, query, fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return helperURI.toString().replace(HOST_TEMPLATE, authority);
    }

    public static String buildURIToString(String scheme, String userInfo, String host, int port, String path, String query,
            String fragment) {
        URI helperURI = null;
        try {
            helperURI = new URI(scheme, userInfo, HOST_TEMPLATE, port, path, query, fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return helperURI.toString().replace(HOST_TEMPLATE, host);
    }

    public NetworkInterfaceURI(String uri) throws IllegalArgumentException {
        Parser parser = new Parser(uri);
        parser.parse();
    }

    static NetworkInterfaceURI create(String str) {
        return new NetworkInterfaceURI(str);
    }

    /**
     * Method retrieving host.
     * @return - host in uri
     */
    public String getHost() {
        return host;
    }

    /**
     * Method retrieving scheme.
     * @return - scheme in uri
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Method retrieving authority.
     * @return - authority in uri
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Method retrieving fragment.
     * @return - fragment in uri
     */
    public String getFragment() {
        return fragment;
    }

    /**
     * Method retrieving path.
     * @return - path in uri
     */
    public String getPath() {
        return path;
    }

    /**
     * Method retrieving query.
     * @return - query in uri
     */
    public String getQuery() {
        return query;
    }

    /**
     * Method retrieving port.
     * @return - port in uri
     */
    public int getPort() {
        return port;
    }

    /**
     * Method retrieving user info section.
     * @return - user info in uri
     */
    public String getUserInfo() {
        return userInfo;
    }

    /**
     * Method retrieving whether uri is absolute.
     * @return - boolean
     */
    public boolean isAbsolute() {
        return absolute;
    }

    /**
     * Method resolving uris
     * @param uriString
     * @return
     */
    public String resolve(String uriString) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Method modifying URI scheme
     * @param newScheme
     * @return - modified uri
     */
    public String modifyURIScheme(String newScheme) {
        return buildURIFromTokens(newScheme, host, port, path, query, fragment);
    }

    /**
     * Method modifying UrI authority
     * @param newAuthority
     * @return - modified uri
     */
    public String modifyURIAuthority(String newAuthority) {
        return buildURIFromTokens(scheme, newAuthority, path, query, fragment);
    }

    /**
     * Method modifying uri port
     * @param newPort
     * @return - modified uri
     */
    public String modifyURIPort(int newPort) {
        return buildURIFromTokens(scheme, host, newPort, path, query, fragment);
    }

    /**
     * Method modifying uri path
     * @param newPath
     * @return - modified uri
     */
    public String modifyURIPath(String newPath) {
        return buildURIFromTokens(scheme, host, port, newPath, query, fragment);
    }

   /**
    * Parser performing NetworkInterfaceSyntax validation and String tokens extraction
    *
    */
    private class Parser {
        private String uri;
        private String matchedToken;

        public Parser(String uri) {
            this.uri = uri;
        }

        /**
         * Method performing parsing
         */
        private void parse() throws IllegalArgumentException {
            if (!uri.startsWith("tcp://") && !uri.startsWith("udp://")) {
                throw new IllegalArgumentException("Network interface URI syntax should only "
                        + "be applicable for tcp and udp schemes");
            }
            Pattern pattern = Pattern.compile("(\\[{0,1}@[a-zA-Z0-9 :]*\\]{0,1})");
            Matcher matcher = pattern.matcher(uri);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Invalid network interface URI syntax");
            }
            matchedToken = matcher.group(0);
            if (matchedToken.matches(".*:.*:.*")) {
                throw new IllegalArgumentException("Multiple ':' characters within network interface syntax not allowed");
            }
            if (matchedToken.contains(" ") && (!matchedToken.startsWith("[") || !matchedToken.endsWith("]"))) {
                throw new IllegalArgumentException("Network interface syntax host contains spaces but misses bracket(s)");
            }
            mockNetworkInterfaceURI = URI.create(uri.replace(matchedToken, HOST_TEMPLATE));
            populateUriDataFromMockInterfaceURI();
        }

        private void populateUriDataFromMockInterfaceURI() {
            scheme = mockNetworkInterfaceURI.getScheme();
            fragment = mockNetworkInterfaceURI.getFragment();
            authority = mockNetworkInterfaceURI.getAuthority().replace(HOST_TEMPLATE, matchedToken);
            userInfo = mockNetworkInterfaceURI.getUserInfo();
            host = mockNetworkInterfaceURI.getHost().replace(HOST_TEMPLATE, matchedToken);
            port = mockNetworkInterfaceURI.getPort();
            path = mockNetworkInterfaceURI.getPath();
            query = mockNetworkInterfaceURI.getQuery();
            absolute = mockNetworkInterfaceURI.isAbsolute();
        }
    }

    //TODO: Check whether algorithm is correct with java.net.URI
    private String buildURIFromTokens(String scheme, String host, int port, String path,
            String query, String fragment) {
        return scheme + "://" + host + ":" + port + (path.isEmpty() ? "" : "/") + path +
                (query != null ? "?" + query : "") +
                (fragment != null ? "#" + fragment : "");
    }

    //TODO: Check whether algorithm is correct with java.net.URI
    private String buildURIFromTokens(String scheme, String authority, String path,
            String query, String fragment) {
        return scheme + "://" + authority + (path.isEmpty() ? "" : "/") + path +
                (query != null ? "?" + query : "") +
                (fragment != null ? "#" + fragment : "");
    }

}
