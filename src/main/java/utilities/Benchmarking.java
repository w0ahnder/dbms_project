package utilities;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import operator.*;
import common.*;

import javax.management.Query;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.List;


public class Benchmarking {
    public String path;
    public int numtuples;
    public String outpath;
    public Statements statements;
    public QueryPlanBuilder queryPlanBuilder;
    public List<Statement> statementList;



    public Benchmarking(String path, int num, String outpath){
        this.path = path;
        numtuples  = num;
        this.outpath = outpath;
    }
    public void setUP() throws URISyntaxException, IOException, JSQLParserException {

        ClassLoader classLoader = Benchmarking.class.getClassLoader();
        URI path = Objects.requireNonNull(classLoader.getResource("binary_samples/input")).toURI();
        Path resourcePath = Paths.get(path);
        // System.out.println("DB path" + resourcePath.resolve("db").toString());
        DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
        DBCatalog.getInstance().config_file(resourcePath.toString());

        URI queriesFile =
                Objects.requireNonNull(classLoader.getResource("binary_samples/input/queries.sql")).toURI();

        statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
        queryPlanBuilder = new QueryPlanBuilder();
        statementList = statements.getStatements();
    }

    //can create additional methods in DBCatalog to set the types of join and the buffer page size for external sort
    public void runQueries() throws JSQLParserException, URISyntaxException, IOException {
        setUP();


    }

    public void runTNL(){}

    public void runBNL(){}

    public void runSMJ(){}


}

