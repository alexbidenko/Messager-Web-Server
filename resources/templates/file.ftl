<!DOCTYPE html>
<html lang="ru">
<head>
    <title>7winds - чат</title>
    <link href="//fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css">
</head>
<body>
<div class="container-fluid">
    <form action="/post_file" method="post" enctype="multipart/form-data">
        <div class="file-field input-field">
            <div class="btn">
                <span>Фотографии</span>
                <input name="file" type="file" accept="image/*,image/jpeg">
            </div>
            <div class="file-path-wrapper">
                <input class="file-path validate d-block" type="text">
            </div>
        </div>
        <input class="btn" type="submit" value="Отправить">
    </form>
</div>
</body>
</html>