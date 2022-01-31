package javamop.output;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javamop.JavaMOPMain;
import javamop.JavaMOPOptions;
import javamop.helper.IOUtils;
import javamop.helper.MOP_Serialization;
import javamop.parser.SpecExtractor;
import javamop.parser.ast.MOPSpecFile;
import javamop.util.MOPException;
import javamop.util.MOPNameSpace;
import javamop.util.Tool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ProductionSpecsTest {
    public static final String resourcePath = "src" + File.separator + "test" + File.separator
            + "resources" + File.separator;
    public static final String pathToMopFiles = resourcePath + "161-specs" + File.separator;
    public static final String pathToExpectedFiles = resourcePath + "expected-output" + File.separator
            + "161-specs" + File.separator;

    private MOPSpecFile inputAST;

    private String output_AJ_FilePath;
    private String output_RVM_FilePath;

    private String mopFilePath;
    private String specName;

    static {
        JavaMOPMain.options = new JavaMOPOptions();
    }


    public ProductionSpecsTest(String inputMOPFile) {
        this.mopFilePath = inputMOPFile;
        String specFileName = mopFilePath.substring(mopFilePath.lastIndexOf(File.separator) + 1);
        this.specName = specFileName.substring(0, specFileName.lastIndexOf("."));

        try {
            this.inputAST = SpecExtractor.parse(new File(this.mopFilePath));
        } catch (MOPException e) {
            e.printStackTrace();
        }
        this.output_AJ_FilePath = pathToExpectedFiles + specName + "MonitorAspect.aj";
        this.output_RVM_FilePath = pathToExpectedFiles + specName + ".rvm";
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> result = new ArrayList<>();
        for (String file : IOUtils.getFilesInDir(pathToMopFiles)) {
            result.add( new Object[] {file});
        }
        return result;
    }

    @Test
    public void testRVMFileGeneration() throws Exception {
        MOPProcessor processor = new MOPProcessor(this.specName);
        MOPNameSpace.init();

        String actualRVString = IOUtils.deleteNewLines(processor.generateRVFile(this.inputAST));
        actualRVString = Tool.changeIndentation(actualRVString, "", "\t");
        String expectedRVString = IOUtils.deleteNewLines(IOUtils.readFile(this.output_RVM_FilePath));
        expectedRVString = Tool.changeIndentation(expectedRVString, "", "\t");

        assertEquals("The generated RV String for spec " + this.mopFilePath +
                " is not as expected", expectedRVString, actualRVString);
    }

    @Test
    public void generateAJFile() throws Exception {
        MOPProcessor processor = new MOPProcessor(this.specName);
        MOPNameSpace.init();

        String actualAJString = IOUtils.deleteNewLines(processor.generateAJFile(this.inputAST));
        actualAJString = Tool.changeIndentation(actualAJString, "", "\t");
        String expectedAJString = IOUtils.deleteNewLines(IOUtils.readFile(this.output_AJ_FilePath));
        expectedAJString = Tool.changeIndentation(expectedAJString, "", "\t");

        assertEquals("The generated AJ String for spec " + this.mopFilePath +
                " is not as expected", expectedAJString, actualAJString);
    }

}