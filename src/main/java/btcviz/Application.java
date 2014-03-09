package btcviz;

import java.io.File;
import java.io.BufferedReader;

import java.util.logging.Logger;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Application {

    private static final String DB_PATH = "db/btcvizneo4j.db";

    private enum AccountRelationshipTypes implements RelationshipType
    {
        TRANSACTS_WITH
    }

    private static GraphDatabaseService graphDb;

    public static void run(String... args) throws Exception {

        // create property graph database
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );

        // register shutdown hook to prevent database corruption upon interruption
        registerShutdownHook( graphDb );

        try ( Transaction tx = graphDb.beginTx() ) {
            // add uniqueness constraint
            graphDb.schema()
                            .constraintFor( DynamicLabel.label("Account"))
                            .assertPropertyIsUnique("address")
                            .create();

            // commit schema constraint
            tx.success();
        }

        try ( Transaction tx = graphDb.beginTx() ) {

            UniqueFactory<Node> accountFactory = new UniqueFactory.UniqueNodeFactory( graphDb, "accounts")
            {
                @Override
                protected void initialize( Node created, Map<String, Object> properties )
                {
                    created.addLabel( DynamicLabel.label( "Account" ) );
                    created.setProperty( "address", properties.get( "address" ) );
                }                  
            };
            int records = 0;
    
            Collection<File> files = getFolderContents("./json");

            for (Iterator<File> iter = files.iterator(); iter.hasNext();)
            {
                File file = iter.next();
                String filename = file.getName();

                System.out.println("Processing " + filename);

                String s = FileUtils.readFileToString(file);

                JSONObject o = (JSONObject)new JSONTokener(s).nextValue();
                {
                    String[] names = JSONObject.getNames(o);

                    for (String name : names) {
                        if (o.get(name) instanceof JSONArray) {

                            JSONArray a = (JSONArray)o.get(name);
                            for (int i = 0; i < a.length(); i++) {
                                List<String> input_addresses  = new ArrayList<String>();
                                List<String> output_addresses = new ArrayList<String>();

                                System.out.println("**********************************************");
                                JSONObject internal_tx = (JSONObject)a.get(i);
                                String[] tx_names = JSONObject.getNames(internal_tx);
                                for (String tx_name : tx_names) {

                                    if (internal_tx.get(tx_name) instanceof JSONArray) {
                                        // process TRANSACTIONs

                                        JSONArray detail_tx_array = (JSONArray)internal_tx.get(tx_name);
                                        for (int j = 0; j < detail_tx_array.length(); j++) {

                                            JSONObject detail_json = (JSONObject)detail_tx_array.get(j);

                                            if (tx_name.equals("inputs")) {

                                                if (detail_tx_array.get(j) instanceof JSONObject) {

                                                    // retrieve element of array & convert to string
                                                    String tx_inputs = detail_tx_array.opt(j).toString();

                                                    // cast as array for proper parsing
                                                    JSONArray tx_input_array = new JSONArray("[" + tx_inputs + "]");                                                    

                                                    // bypass if value empty for inputs key
                                                    if (tx_input_array.get(0).toString().equals("{}")) {
                                                        System.out.println("inputs element NULL");
                                                    } else {
                                                        String tx_input = tx_input_array.toString().replace("{", "[").replace("}", "]");
                                                        String[] txi   = tx_input.split(":");
                                                        String txii    = tx_input.replace(txi[0],"");
                                                        String[] txiii = txii.split("addr");
                                                        String txiv    = txii.replace(txiii[0],"");
                                                        String[] txv   = txiv.split(":\"");
                                                        String txvi    = txiv.replace(txv[0],"");
                                                        String[] txvii = txvi.split("\"");
                                                        String txviii  = txvi.replace(txvii[0],"");
                                                        String[] txvix = txviii.split(",");
                                                        String input_address = txvix[0].replace("\"","");

                                                        System.out.println("INPUT ADDRESS: " + input_address);

                                                        input_addresses.add(input_address);
                                                    }
                                                }
                                            }

                                            if (tx_name.equals("out")) {
                                                if (detail_tx_array.get(j) instanceof JSONObject) {
                                                    JSONObject tx_output = (JSONObject)detail_tx_array.get(j);
        
                                                    if (tx_output.has("addr")) {
                                                        String output_address = tx_output.get("addr").toString();
                                                        System.out.println("OUTPUT ADDRESS : " + output_address);
                                                        output_addresses.add(output_address);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if ((input_addresses.size() > 0) && (output_addresses.size() > 0)) {
                                        System.out.println("Inputs:  " + input_addresses.size());
                                        System.out.println("Outputs: " + output_addresses.size());

                                        // limit node relationships during preliminary analysis
                                        if (output_addresses.size() > 500) {
                                            System.out.println("Break before committing records");
                                            continue;
                                        }
                                        for (String in_addr : input_addresses) {
                                            //Account in = new Account(in_addr);

                                            for (String out_addr : output_addresses) {

                                                // create property graph representation of entity associated with transaction input
                                                Node inputNode = accountFactory.getOrCreate( "address", in_addr );

                                                // create property graph representation of entity associated with transaction output
                                                Node outputNode = accountFactory.getOrCreate( "address", out_addr );
                                     
                                                // create property graph representation of transaction relationship
                                                Relationship relationshipA = inputNode.createRelationshipTo( outputNode, AccountRelationshipTypes.TRANSACTS_WITH );

                                                // create details of transaction relationship
                                                //relationshipA.setProperty( "timestamp", timestamp );

                                                System.out.println(in_addr + " transacts with " + out_addr);

                                                records++;
                                            }
                                        }
                                        System.out.println(records + " interactions processed.");
                                    }
                                }
                            }
                        }
                    }
                }
                // commit the transactions to the database
                tx.success();
            }
        }
    }

    /**
     * Returns all the files in a directory.
     * 
     * @param dir
     *            - Path to the directory that contains the text documents to be
     *            parsed.
     * @return A collection of File Objects
     */
    public static Collection<File> getFolderContents(String dir)
    {
        // Collect all readable documents
        File file = new File(dir);
        Collection<File> files = FileUtils.listFiles(file, CanReadFileFilter.CAN_READ, DirectoryFileFilter.DIRECTORY);
        return files;
    }

    public static void main(String[] args) throws Exception {
        org.neo4j.kernel.impl.util.FileUtils.deleteRecursively(new File( DB_PATH ));

        //SpringApplication.run(Application.class, args);
        Application.run(args);
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
}
