var taskNum=0;
var limitNum=0;
var VMNum=0;
var HostNum=0;

var taskAttriNum=new Array();
var limitAttriNum=new Array();

function taskOtherClick(x,i,j){
  if(x.value=="Other"){
    $("#task"+i+"Attribute"+j).removeClass("hide");
  }
  else{
    $("#task"+i+"Attribute"+j).addClass("hide");
  }
}

function limitOtherClick(x,i,j){
  if(x.value=="Other"){
    $("#limit"+i+"Attribute"+j).removeClass("hide");
  }
  else{
    $("#limit"+i+"Attribute"+j).addClass("hide");
  }
}

function addTask(){
  taskNum++;
  taskAttriNum.push(new Array());
  taskAttriNum[taskNum-1]=2;
  var html='<div id="task'+(taskNum-1)+'Container" class="task">\
    <h4>task'+(taskNum-1)+'  <span onClick="removeTask('+(taskNum-1)+')" class="glyphicon glyphicon-remove" aria-hidden="true"></span></h4>\
    <div id="task'+(taskNum-1)+'" class="item-container">';
  for(var i=0;i<taskAttriNum[taskNum-1];i++){
    html=html+'<form class="form-inline">\
      <div class="item form-group ">\
        <select id="task'+(taskNum-1)+'Key'+i+'" class="form-control" onchange="taskOtherClick(this,'+(taskNum-1)+','+i+')">\
          <option></option>\
          <option>cloudletLength</option>\
          <option>pesNumber</option>\
          <option>cloudletFileSize</option>\
          <option>cloudletOutputSize</option>\
          <option>utilizationModelCpu</option>\
          <option>utilizationModelRam</option>\
          <option>utilizationModelBw</option>\
          <option>record</option>\
          <option>fatherTaskList</option>\
          <option>fileList</option>\
          <option>Other</option>\
        </select>\
        <input id="task'+(taskNum-1)+'Attribute'+i+'" type="text" class="form-control attribute-input hide" placeholder="Attribute">\
        <input id="task'+(taskNum-1)+'Value'+i+'" type="text" class="form-control" placeholder="Value">\
      </div>\
    </form>';
  }
  html=html+'<button id="addTaskAttribute'+(taskNum-1)+'" onClick="addTaskAttribute('+(taskNum-1)+')" type="button" class="btn btn-default btn-sm"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Attribute</button>\
    </div>\
    </div>';
  $("#addTask").before(html);
}

function removeTask(i){
  $("#task"+i+"Container").remove();
  for(var j=i+1;j<taskNum;j++){
    $("#task"+j+"Container").attr("id","task"+(j-1)+"Container");
    $("#task"+(j-1)+"Container").children("h4").html('task'+(j-1)+'  <span onClick="removeTask('+(j-1)+')" class="glyphicon glyphicon-remove" aria-hidden="true"></span>');
    $("#task"+j).attr("id","task"+(j-1));

    for(var k=0;k<taskAttriNum[j];k++){
      $("#task"+(j)+"Key"+k).attr("id","task"+(j-1)+"Key"+k);
      $("#task"+(j-1)+"Key"+k).attr("onchange","taskOtherClick(this,"+(j-1)+","+k+")");
      $("#task"+(j)+"Attribute"+k).attr("id","task"+(j-1)+"Attribute"+k);
      $("#task"+(j)+"Value"+k).attr("id","task"+(j-1)+"Value"+k);
    }

    $("#addTaskAttribute"+j).attr("id","addTaskAttribute"+(j-1));
    $("#addTaskAttribute"+(j-1)).attr("onClick","addTaskAttribute("+(j-1)+")");
  }
  taskAttriNum.splice(i,1);
  taskNum--;
}

function addTaskAttribute(id){

  $("#addTaskAttribute"+id).before('<form class="form-inline">\
    <div class="item form-group ">\
      <select id="task'+(id)+'Key'+taskAttriNum[id]+'" class="form-control" onchange="taskOtherClick(this,'+(id)+','+taskAttriNum[id]+')">\
      <option></option>\
      <option>cloudletLength</option>\
      <option>pesNumber</option>\
      <option>cloudletFileSize</option>\
      <option>cloudletOutputSize</option>\
      <option>utilizationModelCpu</option>\
      <option>utilizationModelRam</option>\
      <option>utilizationModelBw</option>\
      <option>record</option>\
      <option>fatherTaskList</option>\
      <option>fileList</option>\
      <option>Other</option>\
      </select>\
      <input id="task'+(id)+'Attribute'+taskAttriNum[id]+'" type="text" class="form-control attribute-input hide" placeholder="Attribute">\
      <input id="task'+(id)+'Value'+taskAttriNum[id]+'" type="text" class="form-control" placeholder="Value">\
    </div>\
  </form>');
  taskAttriNum[id]++;
}

function addLimit(){
  limitNum++;
  limitAttriNum.push(new Array());
  limitAttriNum[limitNum-1]=2;
  var html='<div id="limit'+(limitNum-1)+'Container" class="limit">\
    <h4>limit'+(limitNum-1)+'  <span onClick="removeLimit('+(limitNum-1)+')" class="glyphicon glyphicon-remove" aria-hidden="true"></span></h4>\
    <div id="limit'+(limitNum-1)+'" class="item-container">';
  for(var i=0;i<limitAttriNum[limitNum-1];i++){
    html=html+'<form class="form-inline">\
      <div class="item form-group ">\
        <select id="limit'+(limitNum-1)+'Key'+i+'" class="form-control" onchange="limitOtherClick(this,'+(limitNum-1)+','+i+')">\
          <option>Select</option>\
          <option>A</option>\
          <option>B</option>\
          <option>C</option>\
          <option>D</option>\
          <option>E</option>\
          <option>Other</option>\
        </select>\
        <input id="limit'+(limitNum-1)+'Attribute'+i+'" type="text" class="form-control attribute-input hide" placeholder="Attribute">\
        <input id="limit'+(limitNum-1)+'Value'+i+'" type="text" class="form-control" placeholder="Value">\
      </div>\
    </form>';
  }
  html=html+'<button id="addLimitAttribute'+(limitNum-1)+'" onClick="addLimitAttribute('+(limitNum-1)+')" type="button" class="btn btn-default btn-sm"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Attribute</button>\
    </div>\
    </div>';
  $("#addLimit").before(html);
}

function removeLimit(i){
  $("#limit"+i+"Container").remove();
  for(var j=i+1;j<limitNum;j++){
    $("#limit"+j+"Container").attr("id","limit"+(j-1)+"Container");
    $("#limit"+(j-1)+"Container").children("h4").html('limit'+(j-1)+'  <span onClick="removeLimit('+(j-1)+')" class="glyphicon glyphicon-remove" aria-hidden="true"></span>');
    $("#limit"+j).attr("id","limit"+(j-1));

    for(var k=0;k<limitAttriNum[j];k++){
      $("#limit"+(j)+"Key"+k).attr("id","limit"+(j-1)+"Key"+k);
      $("#limit"+(j-1)+"Key"+k).attr("onchange","limitOtherClick(this,"+(j-1)+","+k+")");
      $("#limit"+(j)+"Attribute"+k).attr("id","limit"+(j-1)+"Attribute"+k);
      $("#limit"+(j)+"Value"+k).attr("id","limit"+(j-1)+"Value"+k);
    }

    $("#addLimitAttribute"+j).attr("id","addLimitAttribute"+(j-1));
    $("#addLimitAttribute"+(j-1)).attr("onClick","addLimitAttribute("+(j-1)+")");
  }
  limitAttriNum.splice(i,1);
  limitNum--;
}

function addLimitAttribute(id){

  $("#addLimitAttribute"+id).before('<form class="form-inline">\
    <div class="item form-group ">\
      <select id="limit'+(id)+'Key'+limitAttriNum[id]+'" class="form-control" onchange="limitOtherClick(this,'+(id)+','+limitAttriNum[id]+')">\
        <option>Select</option>\
        <option>A</option>\
        <option>B</option>\
        <option>C</option>\
        <option>D</option>\
        <option>E</option>\
        <option>Other</option>\
      </select>\
      <input id="limit'+(id)+'Attribute'+limitAttriNum[id]+'" type="text" class="form-control attribute-input hide" placeholder="Attribute">\
      <input id="limit'+(id)+'Value'+limitAttriNum[id]+'" type="text" class="form-control" placeholder="Value">\
    </div>\
  </form>');
  limitAttriNum[id]++;
}

function addVM(){
  var html='<div id="VM'+(VMNum)+'Container" class="VM">\
    <h4>VM'+VMNum+'  <span onClick="removeVM('+(VMNum)+')" class="glyphicon glyphicon-remove" aria-hidden="true"></span></h4>\
    <div id="VM'+VMNum+'" class="item-container">\
      <form class="form-inline">\
        <div class="item form-group ">\
          <div>\
            <label  class="control-label">size</label>\
            <input id="VM'+VMNum+'size" type="text" class="form-control">\
          </div>\
          <div>\
            <label  class="control-label">ram</label>\
            <input id="VM'+VMNum+'ram" type="text" class="form-control">\
          </div>\
          <div>\
            <label  class="control-label">mips</label>\
            <input id="VM'+VMNum+'mips" type="text" class="form-control">\
          </div>\
          <div>\
            <label class="control-label">bw</label>\
            <input id="VM'+VMNum+'bw" type="text" class="form-control">\
          </div>\
          <div>\
            <label  class="control-label">pesNumber</label>\
            <input id="VM'+VMNum+'pesNumber" type="text" class="form-control">\
          </div>\
          <div>\
            <label  class="control-label">vmm</label>\
            <input id="VM'+VMNum+'vmm" type="text" class="form-control">\
          </div>\
          <div>\
            <label  class="control-label">CloudletScheduler</label>\
            <input id="VM'+VMNum+'CloudletScheduler" type="text" class="form-control">\
          </div>\
        </div>\
      </form>\
    </div>\
  </div>';
  $("#addVM").before(html);
  VMNum++;
}

function removeVM(i){
  $("#VM"+i+"Container").remove();
  for(var j=i+1;j<VMNum;j++){
    $("#VM"+j+"Container").attr("id","VM"+(j-1)+"Container");
    $("#VM"+(j-1)+"Container").children("h4").html('VM'+(j-1)+'  <span onClick="removeVM('+(j-1)+')" class="glyphicon glyphicon-remove" aria-hidden="true"></span>');
    $("#VM"+j).attr("id","VM"+(j-1));

    $("#VM"+(j)+"size").attr("id","VM"+(j-1)+"size");
    $("#VM"+(j)+"ram").attr("id","VM"+(j-1)+"ram");
    $("#VM"+(j)+"mips").attr("id","VM"+(j-1)+"mips");
    $("#VM"+(j)+"bw").attr("id","VM"+(j-1)+"bw");
    $("#VM"+(j)+"pesNumber").attr("id","VM"+(j-1)+"pesNumber");
    $("#VM"+(j)+"vmm").attr("id","VM"+(j-1)+"vmm");
    $("#VM"+(j)+"CloudletScheduler").attr("id","VM"+(j-1)+"CloudletScheduler");
  }
  VMNum--;
}

function addHost(){
  var html='<div id="Host'+(HostNum)+'Container" class="Host">\
    <h4>Host'+HostNum+'  <span onClick="removeHost('+(HostNum)+')" class="glyphicon glyphicon-remove" aria-hidden="true"></span></h4>\
    <div id="Host'+HostNum+'" class="item-container">\
      <form class="form-inline">\
        <div class="item form-group ">\
          <div>\
            <label  class="control-label">PE</label>\
            <input id="Host'+HostNum+'PE" type="text" class="form-control">\
          </div>\
          <div>\
            <label  class="control-label">RamProvisioner</label>\
            <input id="Host'+HostNum+'RamProvisioner" type="text" class="form-control">\
          </div>\<div>\
            <label  class="control-label">BwProvisioner</label>\
            <input id="Host'+HostNum+'BwProvisioner" type="text" class="form-control">\
          </div>\<div>\
            <label  class="control-label">storage</label>\
            <input id="Host'+HostNum+'storage" type="text" class="form-control">\
          </div>\<div>\
            <label  class="control-label">VmScheduler</label>\
            <input id="Host'+HostNum+'VmScheduler" type="text" class="form-control">\
          </div>\
        </div>\
      </form>\
    </div>\
  </div>';
  $("#addHost").before(html);
  HostNum++;
}

function removeHost(i){
  $("#Host"+i+"Container").remove();
  for(var j=i+1;j<HostNum;j++){
    $("#Host"+j+"Container").attr("id","Host"+(j-1)+"Container");
    $("#Host"+(j-1)+"Container").children("h4").html('Host'+(j-1)+'  <span onClick="removeHost('+(j-1)+')" class="glyphicon glyphicon-remove" aria-hidden="true"></span>');
    $("#Host"+j).attr("id","Host"+(j-1));

    $("#Host"+(j)+"PE").attr("id","Host"+(j-1)+"PE");
    $("#Host"+(j)+"RamProvisioner").attr("id","Host"+(j-1)+"RamProvisioner");
    $("#Host"+(j)+"BwProvisioner").attr("id","Host"+(j-1)+"BwProvisioner");
    $("#Host"+(j)+"storage").attr("id","Host"+(j-1)+"storage");
    $("#Host"+(j)+"VmScheduler").attr("id","Host"+(j-1)+"VmScheduler");
  }
  HostNum--;
}

function execute(){

  var summary=new Object();
  summary.VMs=new Array();
  for(var i=0;i<VMNum;i++){
    var vm=new Object();
    vm.ref=""+i;
    vm.size=$("#VM"+(i)+"size").val();
    vm.ram=$("#VM"+(i)+"ram").val();
    vm.mips=$("#VM"+(i)+"mips").val();
    vm.bw=$("#VM"+(i)+"bw").val();
    vm.pesNumber=$("#VM"+(i)+"pesNumber").val();
    vm.vmm=$("#VM"+(i)+"vmm").val();
    vm.CloudletScheduler=$("#VM"+(i)+"CloudletScheduler").val();
    summary.VMs.push(vm);
  }

  summary.Task=new Object();
  summary.Task.ref="0";
  summary.Task.arrivalTime=new Date();
  summary.Task.deadline="";
  summary.Task.Subtask=new Array();

  for(var i=0;i<taskNum;i++){
    var task=new Object();
    task.ref=""+i;
    console.log(taskAttriNum[i]);
    for(var j=0;j<taskAttriNum[i];j++){
      if($("#task"+i+"Key"+j+" option:selected").text()=="Other"){
        if($("#task"+i+"Attribute"+j).val()!=""){
            task[$("#task"+i+"Attribute"+j).val()]=$("#task"+i+"Value"+j).val();
        }

      }
      else{
        if($("#task"+i+"Key"+j+" option:selected").text()!=""){
          task[$("#task"+i+"Key"+j+" option:selected").text()]=$("#task"+i+"Value"+j).val();
        }
      }
    }
    summary.Task.Subtask.push(task);
  }

  summary.Datacenter=new Object();
  summary.Datacenter.ref="0";
  summary.Datacenter.arch=$("#datacenter_arch").val();
  summary.Datacenter.os=$("#datacenter_os").val();
  summary.Datacenter.vmm=$("#datacenter_vmm").val();
  summary.Datacenter.time_zone=$("#datacenter_time_zone").val();
  summary.Datacenter.cost=$("#datacenter_cost").val();
  summary.Datacenter.costPerMem=$("#datacenter_costPerMem").val();
  summary.Datacenter.costPerStorage=$("#datacenter_costPerStorage").val();
  summary.Datacenter.costPerBw=$("#datacenter_costPerBw").val();
  summary.Datacenter.storageList=$("#datacenter_storageList").val();
  summary.Datacenter.Host=new Array();

  for(var i=0;i<HostNum;i++){
    var host=new Object();
    host.ref=""+i;
    host.PE=$("#Host"+(i)+"PE").val();
    host.RamProvisioner=$("#Host"+(i)+"RamProvisioner").val();
    host.BwProvisioner=$("#Host"+(i)+"BwProvisioner").val();
    host.storage=$("#Host"+(i)+"storage").val();
    host.VmScheduler=$("#Host"+(i)+"VmScheduler").val();
    summary.Datacenter.Host.push(host);
  }

  var json = JSON.stringify(summary);
  $("#log").html('Please wait for the simulation result...');
  $.ajax({
        type: 'post',
        url:  '/web/scheduler',
        data: json,
        dataType: 'json',
        success: function (data) {
            console.log("Hey, we got reply form java side, with following data: ");
            $("#log").html("<p>Success:"+data.success+"</p>");

            // redirecting example..


        }
    });
}

$(document).ready(function(){
/*
  for(var i=0;i<taskNum;i++){
    $("#tasks").after('<div class="task">\
      <h4>task1</h4>\
      <div id="task1" class="item-container">\
        <form class="form-inline">\
          <div class="item form-group ">\
            <select id="task1Key1" class="form-control" onchange="taskOtherClick(this)">\
              <option>Select</option>\
              <option>A</option>\
              <option>B</option>\
              <option>C</option>\
              <option>D</option>\
              <option>E</option>\
              <option>Other</option>\
            </select>\
            <input id="task1Attribute1" type="text" class="form-control attribute-input hide" placeholder="Attribute">\
            <input id="task1Value1" type="text" class="form-control" placeholder="Value">\
          </div>\
        </form>\
        <button id="addAttribute1" onClick="addAttribute(\'addAttribute1\')" type="button" class="btn btn-default btn-sm"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Attribute</button>\
      </div>\
    </div>');
  }
  */

    $("#upButton").click(function () {


        var $input = $('#TForm');
        // 相当于： $input[0].files, $input.get(0).files
        var files = $input.prop('files');
        console.log(files[0].name);

        var uploadData = new FormData(document.getElementById('upForm'));
        // uploadData.append('file', files[0]);

        $.ajax({
            url: '/upload',
            type: 'POST',
            data: uploadData,
            cache: false,
            processData: false,
            contentType: false,
            success: function (data) {
                //console.log("Hey, we got reply form java side, with following xml: ");
                alert("Hey, we got reply form java side, with following xml: "+data);

                // redirecting example..
            }
        });
    });
});
