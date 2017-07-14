var exec = require('cordova/exec');  
var myFunc = function(){};
// arg1：成功回调
// arg2：失败回调
// arg3：将要调用类配置的标识(Plugin.xml中的feature对应的name)
// arg4：调用的原生方法名
// arg5：参数，json格式

myFunc.prototype.getPhoneInfo = function(arg0, success, error) {  
    exec(success, error, "PhoneInfoPlugin", "getPhoneInfo", [arg0]);
};  

var showFunc = new myFunc();
module.exports = showFunc;
