##说明:
* 一个简化RxJava的模仿器,简单快捷省代码,只适合需要异步调用线程处理数据的调度,复杂的请使用rxJava或rxJava
*使用方法:


        方法一:
        RxThread.single()//单线程
                 //io()//io线程
                 //scheduled()//计划线程
                 //newThread()//新线程
                .observeOnMain()//线程结束回调在主线程
                //.observeOnSync()//线程结束回调在同步线程(子线程)
                .observe(new RxSubscribe() {//设置线程回调监听器
                    public void onError(String tag,Throwable t) {
                        //线程错误
                    }
        
                    @Override
                    public void onCompleted(String tag) {
                        //线程完成
                    }
        
                    @Override
                    public void onStart(String tag) {
                        //线程开始
                    }
                })
                .tag("tag")//设置线程的标记
                .priority(Thread.MAX_PRIORITY)//设置线程的优先级
                .open()
                .delay(1000,TimeUnit.MILLISECONDS)//设置延时
                .execute(new Runnable() {//普通的一个线程启用
                    @Override
                    public void run() {
        
                    }
                });
                
         方法二:
        RxThread.single()//单线程
                 //io()//io线程
                 //scheduled()//计划线程
                 //newThread()//新线程
                 .observeOnMain()//线程结束回调在主线程
                 //.observeOnSync()//线程结束回调在同步线程(子线程)
                 .observe(new RxSubscribe() {//设置线程回调监听器
                     public void onError(String tag,Throwable t) {
                         //线程错误
                     }
         
                     @Override
                     public void onCompleted(String tag) {
                         //线程完成
                     }
         
                     @Override
                     public void onStart(String tag) {
                         //线程开始
                     }
                 })
                 .tag("tag")//设置线程的标记
                 .priority(Thread.MAX_PRIORITY)//设置线程的优先级
                 .open()
                 .delay(1000,TimeUnit.MILLISECONDS)//设置延时
                 .subscriber(new RxSubscriber<Integer>() {//线程发射器,在接收器中接收,每个接收器都会按照发射顺序接收
                     @Override
                     public void onSubscribe(RxEmitter<Integer> emitter) {
                         emitter.onNext(0);//发射数据一
                         emitter.onNext(1);//发射数据二
                     }
                 })
                 .acceptOnMain()//接收器在主线程
                 //.acceptOnSync()//接收器在同步线程
                 .acceptor(new RxAcceptor<Integer>() {
                     @Override
                     public void onStart() {
                           //接收器开启,与线程监听器为同一线程,在observeOnMain/observeOnSync中指定
                     }
                 
                     @Override
                     public void onNext(Integer integer) {
                            //接收器开启,与接收器为同一线程,acceptOnMain/acceptOnSync指定
                     }
                 
                     @Override
                     public void onError(Throwable t) {
                            //接收器开启,与线程监听器为同一线程,在observeOnMain/observeOnSync中指定
                     }
                 
                     @Override
                     public void onComplete() {
                            //接收器开启,与线程监听器为同一线程,在observeOnMain/observeOnSync中指定
                     }
                 });