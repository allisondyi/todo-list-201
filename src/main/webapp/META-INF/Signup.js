import './Signup.css'

import React, { useState } from 'react';

const CreateAccount = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleCreateAccount = () => {
    console.log(`Email: ${email}, Password: ${password}`);
  };

  // CSS STYLES

  const headerStyle = {
    background: '#A795FF', 
    padding: '20px', 
    textAlign: 'left', 
    color: 'white !important', 
    };

  const bodyStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    height: '100vh',
    margin: '0',
    padding: '0',
    background: '#A795FF',
  };

  const formStyle = {
    background: 'white',
    padding: '20px',
    borderRadius: '8px',
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
    width: '480px', 
    height: '540px',
    

  };

  const inputStyle = {
    border: '1px solid #ccc',
    borderRadius: '4px',
    padding: '10px',
    fontSize: '16px',
  };

  const labelStyle = {
    fontSize: '14px',
    color: '#333',
    marginBottom: '5px',
  };

  const buttonStyle = {
    color: 'white',
    border: 'none',
    cursor: 'pointer',
    background:'#2B1887',
    width:'384px',
    height:'56px',
    marginTop:'14px',
    alignItems: 'center',
    marginLeft:'48px',
  };

  return (
    <div>
      <div style={headerStyle}>
      <h1 style={{ color: 'white' }}>To do list</h1>
      </div>
      <div style={bodyStyle}>
        <div style={formStyle}>
          <div style={{ ...labelStyle, textAlign: 'center', fontSize: '40px', fontWeight:'bold', marginTop:'48px' }}>
            Welcome Back!
          </div>
          <form>
            <div style={{ display: 'flex', flexDirection: 'column', marginLeft:'48px', marginRight:'48px',marginTop:'48px',}}>
              <label style={labelStyle}>
                EMAIL*
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={inputStyle}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' , marginLeft:'48px', marginRight:'48px', marginTop:'24px',}}>
              <label style={labelStyle}>
                PASSWORD*
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={inputStyle}
              />
            </div>
            <label style={{ display: 'flex', alignItems: 'center',  marginLeft:'48px', marginRight:'48px', marginTop:'24px', fontSize:'12px',}}>
             Forgot your password?
            </label>
            <button type="button" onClick={handleCreateAccount} style={buttonStyle}>
              Sign In
            </button>
            <button type="button" onClick={handleCreateAccount} style={buttonStyle}>
              Continue as Guest
            </button>
            <div style={{ textAlign: 'center', marginTop: '10px', fontSize: '14px' }}>
            Don't have an account yet?  <span style={{ color: '#4C3A51', cursor: 'pointer' }}>Sign up now</span>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateAccount;
