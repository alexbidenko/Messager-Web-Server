<html>
<head>
    
</head>
<body>
<#if error??>
    <p style="color:red;">${error}</p>
</#if>
<form action="/login" method="post" enctype="application/x-www-form-urlencoded">
    <div>Логин:</div>
    <div><input type="text" name="username" /></div>
    <div>Пароль:</div>
    <div><input type="password" name="password" /></div>
    <div>Повторите пароль:</div>
    <div><input type="password" name="repeat_password" /></div>
    <div><input type="submit" value="Войти" /></div>
</form>
</body>
</html>