package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

/** The suite of all JUnit tests for the gitlet package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    @Test
    public void testTimeFormat() {
        Commit commit = new Commit("GG", "");
        System.out.println(commit.getZonedTime());
    }

    @Test
    public void testGetAllFiles() {
        Repository repo = new Repository();
        repo.getAllFiles(new File("."))
            .stream().forEach(f -> System.out.println(f));
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }

}


