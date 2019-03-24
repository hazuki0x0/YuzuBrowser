setTimeout("transfer()", 0);
function transfer(){
location.replace("../en/" + window.location.pathname.split("/").pop());
}