<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%--<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://"
          + request.getServerName() + ":" + request.getServerPort()
          + path + "/";
%>--%>
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- 上述3个meta标签*必须*放在最前面，任何其他内容都*必须*跟随其后！ -->
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Scheduling Simulator</title>

    <!-- Bootstrap core CSS -->
    <link href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">

    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <link href="./css/ie10-viewport-bug-workaround.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="./css/main.css" rel="stylesheet">

    <!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
    <!--[if lt IE 9]><script src="../../assets/js/ie8-responsive-file-warning.js"></script><![endif]-->
    <script src="./js/ie-emulation-modes-warning.js"></script>

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://cdn.bootcss.com/html5shiv/3.7.3/html5shiv.min.js"></script>
      <script src="https://cdn.bootcss.com/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>

  <body>

    <nav class="navbar navbar-inverse">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand">Scheduling Simulator</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
          <ul class="nav navbar-nav navbar-right">
            <li class=""><a href="#">Log in</a></li>
            <li class="nav-line">|</li>
            <li class=""><a href="#">Log out</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </nav>

    <div class="container main-part">
      <div class="row">
        <div class="area col-md-6 col-sm-12">
          <h1 id="tasks">TM</h1>
          <button id="addTask" onClick="addTask()" type="button" class="btn btn-default btn-sm"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Task</button>
          <form name="demoForm"  id="upForm" enctype="multipart/form-data" method="post">
            <p>Upload File: <input type="file" id="TForm" name="taskfile"></p>
              <p><button type="button" id="upButton" value="Submit" class="btn btn-default btn-sm">Upload</button></p>
          </form>
          <%--<button id="addTaskFile" onClick="addTask()" type="button" class="btn btn-default btn-sm"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Upload Task File</button>--%>
        </div>
        <div class="area col-md-6 col-sm-12">
          <h1 id="limits">LMC</h1>
          <button id="addLimit" onClick="addLimit()" type="button" class="btn btn-default btn-sm"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Limit</button>
        </div>
      </div>
      <div class="row">
        <div class="area col-md-6 col-sm-12">
          <h1 id="VMs">Virtual Machine</h1>
          <button id="addVM" onClick="addVM()" type="button" class="btn btn-default btn-sm"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> VM</button>
        </div>
        <div class="area col-md-6 col-sm-12">
          <h1 id="DataHosts">Datacenter</h1>
          <form class="form-inline">
            <div class="item form-group ">
              <div>
                <label  class="control-label">arch</label>
                <input id="datacenter_arch" type="text" class="form-control">
              </div>
              <div>
                <label  class="control-label">os</label>
                <input id="datacenter_os" type="text" class="form-control">
              </div>
              <div>
                <label  class="control-label">vmm</label>
                <input id="datacenter_vmm" type="text" class="form-control">
              </div>
              <div>
                <label  class="control-label">time_zone</label>
                <input id="datacenter_time_zone" type="text" class="form-control">
              </div>
              <div>
                <label  class="control-label">cost</label>
                <input id="datacenter_cost" type="text" class="form-control">
              </div>
              <div>
                <label  class="control-label">costPerMem</label>
                <input id="datacenter_costPerMem" type="text" class="form-control">
              </div>
              <div>
                <label  class="control-label">costPerStorage</label>
                <input id="datacenter_costPerStorage" type="text" class="form-control">
              </div>
              <div>
                <label  class="control-label">costPerBw</label>
                <input id="datacenter_costPerBw" type="text" class="form-control">
              </div>
              <div>
                <label  class="control-label">storageList</label>
                <input id="datacenter_storageList" type="text" class="form-control">
              </div>
            </div>
          </form>
          <button id="addHost" onClick="addHost()" type="button" class="btn btn-default btn-sm"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Host</button>
        </div>
      </div>
      <div class="row">
        <div class="area col-md-6 col-sm-12">
          <h1 id="schedule">Scheduling</h1>
          <button id="execute" onClick="execute()" type="button" class="btn btn-success">Execute</button>
          <div id="log">

          </div>
        </div>
      </div>
    </div><!-- /.container -->






    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
    <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <script src="./js/ie10-viewport-bug-workaround.js"></script>
    <script src="./js/main.js?v=1.6"></script>
  </body>
</html>
