
----------使用说明--------------
1 新建工程 P，引入  sb_zjob_client_20230524 依赖
	配置server：
	zjob.server.host=http://localhost
	zjob.server.port=22222
	
2 P 配置 @EnableZJob

3 P 新建A类：
	@ZJobComponent
	public class A  {	

		@ZJobTask(name = "task1")
		public void task1(final TaskParam taskParam) {
			// xxx 
			// xxx
		}
		
	}
	
4 启动 server 和 P，server端配置好 第3步的task1的相关配置(启用状态，cron等)，
	第3步的task1即将按cron执行
