package sqlTsinjo.query.base;

import java.util.function.Function;

public record ParseSuccess<T>(String remaining,T matched ) implements AutoCloseable {

    @Override
    public void close()   {
      
    }

    public<K> ParseSuccess<K> map(Function<T,K> mapper){
        return new ParseSuccess<K>(remaining , mapper.apply(this.matched) );
    }

}
