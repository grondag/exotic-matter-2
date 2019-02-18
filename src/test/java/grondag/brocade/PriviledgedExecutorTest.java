package grondag.exotic_matter;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import grondag.exotic_matter.concurrency.PrivilegedExecutor;

public class PriviledgedExecutorTest
{
    HashSet<Runnable> plain = new HashSet<Runnable>();
    HashSet<Runnable> privileged = new HashSet<Runnable>();
    PrivilegedExecutor executor = new PrivilegedExecutor("blort!");
    @Test
    public void test()
    {   
        // should be generally true: priviledged tasks run before unpriv
        // should always be true: tasks run in the executors's thread

        assert executor.threadName == "blort!";
        plain.clear();
        privileged.clear();
        
        for(int i = 0; i < 1000; i++)
        {
            Runnable rPlain = new Runnable()
            {
                @Override
                public void run()
                {
                    assert Thread.currentThread().getName().startsWith(executor.threadName);
                            
                    try { Thread.sleep(10);} catch (InterruptedException e) { }
                    
                    plain.remove(this);
                    
                    if(plain.isEmpty())
                    {
                        System.out.println(String.format("Plain tasks are done with %d in privileged set. (Should be zero)", privileged.size()));
                        assert privileged.isEmpty();
                    }
                }
            };
            
            Runnable rSpecial = new Runnable()
            {
                @Override
                public void run()
                {
                    assert Thread.currentThread().getName().startsWith(executor.threadName);
                            
                    try { Thread.sleep(10);} catch (InterruptedException e) { }
                    
                    privileged.remove(this);
                    
                    if(privileged.isEmpty())
                    {
                        System.out.println(String.format("Privileged tasks are done with %d in plain set. (Should be > 500)", plain.size()));
                        assert plain.size() > 500;
                    }
                }
            };
            
            this.plain.add(rPlain);
            this.privileged.add(rSpecial);
            this.executor.execute(rPlain, false);
            this.executor.submit(rSpecial, true);
        }
        this.executor.shutdown();
        try
        {
            this.executor.awaitTermination(120, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        assert this.plain.isEmpty();
        assert this.privileged.isEmpty();
    }

}
