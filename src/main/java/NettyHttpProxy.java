import com.fibbery.handler.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * http代理
 *
 * @author fibbery
 * @date 18/1/17
 */
public class NettyHttpProxy {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ServerChannelInitializer());
        ChannelFuture future = bootstrap.bind("10.1.12.90",9999).sync();
        System.out.println("服务器启动，端口8080");
        future.channel().closeFuture().sync();
    }
}
