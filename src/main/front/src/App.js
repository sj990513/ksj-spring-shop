import React from 'react';
import './App.css';
import { BrowserRouter as Router, Route, Switch, Link, NavLink } from 'react-router-dom';
import MainScreen from './components/MainScreen';
import Login from './components/Login';
import Signup from './components/Signup';
import Logout from './components/Logout';
import { AuthProvider, AuthContext } from './context/AuthContext';

const onNaverLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/naver";
};

const onGoogleLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/google";
};

const getData = () => {
  fetch("http://localhost:8080", {
    method: "GET",
    credentials: 'include'
  })
    .then((res) => res.json())
    .then((data) => {
      alert(data);
    })
    .catch((error) => alert(error));
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <div>
          <nav>
            <ul>
              <li>
                <NavLink exact to="/" activeClassName="active">Home</NavLink>
              </li>
              <AuthContext.Consumer>
                {({ isLoggedIn }) => (
                  <>
                    {!isLoggedIn && (
                      <>
                        <li>
                          <NavLink to="/login" activeClassName="active">Login</NavLink>
                        </li>
                        <li>
                          <NavLink to="/signup" activeClassName="active">Signup</NavLink>
                        </li>
                      </>
                    )}
                    {isLoggedIn && (
                      <li>
                        <Logout />
                      </li>
                    )}
                  </>
                )}
              </AuthContext.Consumer>
            </ul>
          </nav>
          <Switch>
            <Route exact path="/" component={MainScreen} />
            <Route path="/login" component={Login} />
            <Route path="/signup" component={Signup} />
          </Switch>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;