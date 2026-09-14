const App = () => {
    return (
        <div className="admin-layout">
            <div className="sidebar" style={{width: '300px'}}>
                <div className="sidebar-header">
                    <h1><i className="fa-solid fa-train-subway" style={{color: 'var(--accent-gold)'}}></i> LuxRail Ops</h1>
                    <div style={{marginTop: '0.5rem', color: 'rgba(255,255,255,0.7)', fontSize: '0.9rem'}}>Carriage & Seat Management Module</div>
                </div>
                <div style={{flexGrow: 1, padding: '1rem 0'}}>
                    <div className="nav-item active">
                        <i className="fa-solid fa-chair" style={{width: '24px'}}></i>
                        Carriages & Inventory
                    </div>
                </div>
                <div className="sidebar-footer">
                    <div style={{fontWeight: 600}}>System Administrator</div>
                    <div style={{fontSize: '0.8rem', color: 'rgba(255,255,255,0.5)'}}>Admin Access</div>
                </div>
            </div>

            <div className="main-content" style={{marginLeft: '300px'}}>
                <div style={{display: 'flex', justifyContent: 'flex-end', marginBottom: '2rem'}}>
                    <div className="card" style={{padding: '0.75rem 1.5rem', marginBottom: 0, borderRadius: '99px', display: 'inline-block'}}>
                        <i className="fa-solid fa-shield-halved" style={{color: 'var(--accent-gold)', marginRight: '0.5rem'}}></i> 
                        Authorized Personnel Only
                    </div>
                </div>
                <Carriages />
            </div>
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
