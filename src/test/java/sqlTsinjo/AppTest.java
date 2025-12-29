package sqlTsinjo;
import com.fasterxml.jackson.databind.ObjectMapper;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.main.insert.element.classes.InsertRqstDefaultValues;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     * @throws ParseNomException 
     */
    @Test
    public void shouldAnswerWithTrue() throws ParseNomException {
        System.out.println(""+InsertRqstDefaultValues.parseMultipleValuesRows("('tay',null,1) "));
        assertTrue(true);
    }
}
