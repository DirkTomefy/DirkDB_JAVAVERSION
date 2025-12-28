package sqlTsinjo.query.base;
public record ParseSuccess<T>(String remaining,T matched ) implements AutoCloseable {

    @Override
    public void close()   {
      
    }

}
