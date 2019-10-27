<!DOCTYPE html>
<html lang="ru">
<head>
    <title>7winds - чат</title>
    <link rel="stylesheet" href="/static/styles.css"/>
    <script src="https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.3.1.min.js"></script>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">

    <link href="//fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css">

    <script src="//cdn.jsdelivr.net/npm/vue@2.6.6/dist/vue.js"></script>
</head>
<body>
<div id="chat-app" class="container-fluid p-0" style="min-height: 100%;">
    <h1 class="m-0">Ваш логин: {{me}}</h1>
    <div class="row m-0">
        <div class="col-4 alert alert-dark rounded-0">
            Активные пользователи
        </div>
        <div class="col-8 alert alert-dark rounded-0">
            Сообщения с пользователем
        </div>
    </div>
    <div class="row">
        <div id="users" class="col-4 p-0 light-blue lighten-5">
            <div v-show="(count_chats > 1)" :class="{'alert-info': 'to_all'!=speaker, 'alert-warning': 'to_all'==speaker}"
                 class="alert alert-info users-block" @click="ChangeChat(this, 'to_all')">
                Написать всем<span v-show="(all_chats.to_all.new_mess > 0)" class="new badge">{{all_chats.to_all.new_mess}}</span>
            </div>
            <div v-for="(chat, user, index) in all_chats" v-if="user != 'to_all'" class="alert users-block"
                 :class="{'alert-info': user!=speaker, 'alert-warning': user==speaker}" @click="ChangeChat(this, user)">
                {{user}} - {{(chat.is_online)?'Онлайн':'Офлайн'}}<span v-show="(chat.new_mess > 0)" class="new badge">{{chat.new_mess}}</span>
            </div>
        </div>
        <div id="messages" class="col-8">
            <div v-if="!!speaker" class="w-100">
                <div v-for="(message, index) in all_messages" :key="index" class="row"
                     :class="{'justify-content-end': message.owner == me}">
                    <div class="col-8 alert" :class="(message.owner == me ? 'alert-success' : 'alert-info')">
                        <small v-if="(speaker=='to_all')">От {{message.owner}}</small>
                        <p v-if="!!message.message">{{message.message}}</p>
                        <img v-if="!!message.image" :src="message.image" class="img-fluid">
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div id="input-panel" class="container-fluid position-fixed yellow lighten-5 p-0" style="bottom: 0;">
        <div class="row m-0">
            <div class="col-10 p-0">
                <div class="input-field">
                    <textarea id="text-message" class="materialize-textarea" @keydown.enter.prevent="SendMessage"></textarea>
                    <label for="text-message">Текст сообщения:</label>
                </div>
            </div>
            <div class="col-2 p-0">
                <button class="btn btn-block" @click="SendMessage" :disabled="!speaker">Отправить</button>
                <div class="file-field input-field">
                    <div class="btn">
                        <span>Фотографии</span>
                        <input name="file" type="file" accept="image/*,image/jpeg" onchange="SendPhoto(this);">
                    </div>
                    <div class="file-path-wrapper">
                        <input class="file-path validate d-block" type="text">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js"></script>

<script src="/static/chat.js"></script>
</body>
</html>