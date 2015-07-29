# jest-for-appengine
A small shim (hack) that allows you to use [Jest](https://github.com/searchbox-io/Jest) (and therefore, [Elasticsearch](https://www.elastic.co/products/elasticsearch)) from [Google App Engine](https://cloud.google.com/appengine/docs)

## Why?
If you want to talk to Elasticsearch from Google App Engine, you can't use the official java client because it tries to add the machine as a node to the cluster which doesn't end well. Jest is an awesome library that instead talks to the remote Elasticsearch using only the REST API. This sounds great, but also has dependencies on classes that cannot be run in an app engine environment.

## How to use it
In your gradle.build, include it as a dependency along with your Jest dependencies:

    compile files('path/to/jest-for-appengine-0.0.1.jar')
    compile 'io.searchbox:jest:0.1.6'
    compile 'org.slf4j:slf4j-log4j12:1.7.12'
    compile 'org.elasticsearch:elasticsearch:1.4.4'
    
In your `appengine-web.xml` you'll need to prioritise that jar in the classloader:

    <class-loader-config>
        <priority-specifier filename="jest-for-appengine-0.0.1.jar"/>
    </class-loader-config>
    

Then you [follow the Jest instructions](https://github.com/searchbox-io/Jest/tree/master/jest) except you need to use a `JestForAppEngineClient` as your `JestClient` eg:

    JestClient client = new JestForAppEngineClient(new URL("http://yourserver.com:9200/"));

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchQuery(field, value));

    Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex("twitter")
                .build();

    SearchResult result = client.execute(search);
    
This whole project is a hideous hack and I'd love to delete it and point people to a better solution. Suggestions welcome... ;)
