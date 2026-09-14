const Carriages = () => {
    const [carriages, setCarriages] = React.useState([]);
    const [trains, setTrains] = React.useState([]);
    const [selectedCarriage, setSelectedCarriage] = React.useState(null);
    const [seats, setSeats] = React.useState([]);
    const [showAddModal, setShowAddModal] = React.useState(false);

    // MOCKED TRAINS since the Train module has been removed
    const mockTrains = [
        { trainId: 101, trainName: 'Yal Devi', trainNumber: 'TR-101' },
        { trainId: 102, trainName: 'Udarata Menike', trainNumber: 'TR-102' },
        { trainId: 103, trainName: 'Rajarata Rejini', trainNumber: 'TR-103' },
        { trainId: 104, trainName: 'Galu Kumari', trainNumber: 'TR-104' }
    ];

    React.useEffect(() => {
        fetchCarriages();
        setTrains(mockTrains);
    }, []);

    const fetchCarriages = async () => {
        const res = await axios.get('/api/carriages');
        setCarriages(res.data);
    };

    const fetchSeats = async (carriageId) => {
        const res = await axios.get(`/api/seats/carriage/${carriageId}`);
        setSeats(res.data);
    };

    const handleSelectCarriage = (c) => {
        setSelectedCarriage(c);
        fetchSeats(c.carriageId);
    };

    const toggleSeatStatus = async (seat) => {
        const newStatus = seat.status === 'Active' ? 'Inactive' : 'Active';
        await axios.put(`/api/seats/${seat.seatId}/status`, { status: newStatus });
        fetchSeats(selectedCarriage.carriageId);
    };

    const handleAddCarriage = async (e) => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target));
        data.trainId = parseInt(data.trainId);
        data.capacity = parseInt(data.capacity);
        data.status = 'Active';
        try {
            await axios.post('/api/carriages', data);
            setShowAddModal(false);
            fetchCarriages();
        } catch (err) { alert('Failed to add carriage. Number might not be unique.'); }
    };

    const deactivate = async (c, e) => {
        e.stopPropagation();
        if(confirm('Deactivate carriage?')) {
            await axios.delete(`/api/carriages/${c.carriageId}`);
            fetchCarriages();
            if (selectedCarriage?.carriageId === c.carriageId) fetchSeats(c.carriageId);
        }
    };

    const reactivate = async (c, e) => {
        e.stopPropagation();
        if(confirm('Reactivate carriage?')) {
            await axios.put(`/api/carriages/${c.carriageId}/reactivate`);
            fetchCarriages();
            if (selectedCarriage?.carriageId === c.carriageId) fetchSeats(c.carriageId);
        }
    };

    const hardDelete = async (c, e) => {
        e.stopPropagation();
        if(confirm('WARNING: Permanently delete carriage and all its seats?')) {
            await axios.delete(`/api/carriages/${c.carriageId}/hard`);
            fetchCarriages();
            if (selectedCarriage?.carriageId === c.carriageId) {
                setSelectedCarriage(null);
                setSeats([]);
            }
        }
    };

    const renderSeatLayout = () => {
        if (!selectedCarriage || seats.length === 0) return <div className="empty-state">No seats.</div>;
        let cols = selectedCarriage.classType === 'First Class' ? 2 : selectedCarriage.classType === 'Second Class' ? 3 : 4;
        const rows = [];
        for (let i = 0; i < seats.length; i += cols) rows.push(seats.slice(i, i + cols));

        return (
            <div className="seat-layout">
                {rows.map((row, rowIndex) => (
                    <div key={rowIndex} className="seat-row">
                        {row.map((seat, seatIndex) => (
                            <React.Fragment key={seat.seatId}>
                                <div className={`seat ${seat.status.toLowerCase()}`} onClick={() => toggleSeatStatus(seat)}>
                                    {seat.seatNumber.split('-')[1]}
                                    <div className="seat-tooltip">
                                        {seat.seatNumber} ({seat.seatType}) - {seat.status}
                                    </div>
                                </div>
                                {(cols === 4 && seatIndex === 1) && <div className="aisle-gap"></div>}
                                {(cols === 3 && seatIndex === 0) && <div className="aisle-gap"></div>}
                                {(cols === 2 && seatIndex === 0) && <div className="aisle-gap"></div>}
                            </React.Fragment>
                        ))}
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div style={{display: 'grid', gridTemplateColumns: '350px 1fr', gap: '2rem'}}>
            <div className="card">
                <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem'}}>
                    <h2 style={{margin: 0}}><i className="fa-solid fa-list"></i> Carriages</h2>
                    <button className="btn btn-primary" onClick={() => setShowAddModal(true)}><i className="fa-solid fa-plus"></i></button>
                </div>
                <div className="carriage-list">
                    {carriages.map(c => (
                        <div key={c.carriageId} className={`carriage-item ${selectedCarriage?.carriageId === c.carriageId ? 'active' : ''}`} onClick={() => handleSelectCarriage(c)}>
                            <div className="carriage-info">
                                <h3>{c.carriageNumber}</h3>
                                <div className="carriage-meta">
                                    <span>{c.classType}</span>
                                    <span>{c.capacity} Seats</span>
                                </div>
                            </div>
                            <div style={{display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem'}}>
                                <span className={`badge ${c.status.toLowerCase()}`}>{c.status}</span>
                                <div style={{display: 'flex', gap: '0.5rem'}}>
                                    {c.status === 'Active' ? (
                                        <button className="btn btn-warning" style={{padding: '0.2rem 0.5rem', fontSize: '0.7rem', backgroundColor: 'var(--warning)'}} onClick={(e) => deactivate(c, e)}>Deactivate</button>
                                    ) : (
                                        <button className="btn btn-success" style={{padding: '0.2rem 0.5rem', fontSize: '0.7rem'}} onClick={(e) => reactivate(c, e)}>Reactivate</button>
                                    )}
                                    <button className="btn btn-danger" style={{padding: '0.2rem 0.5rem', fontSize: '0.7rem'}} onClick={(e) => hardDelete(c, e)}>Delete</button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="card">
                <h2><i className="fa-solid fa-chair"></i> Seat Layout</h2>
                {selectedCarriage ? (
                    <div>
                        <div style={{display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)', marginBottom: '1rem'}}>
                            <span>Train: {trains.find(t=>t.trainId===selectedCarriage.trainId)?.trainName}</span>
                            <span>Class: {selectedCarriage.classType}</span>
                        </div>
                        {renderSeatLayout()}
                    </div>
                ) : <div className="empty-state">Select a carriage</div>}
            </div>

            {showAddModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <h3>Add New Carriage</h3>
                        <form onSubmit={handleAddCarriage}>
                            <div className="form-group">
                                <label>Train</label>
                                <select name="trainId" required>
                                    <option value="">Select Train...</option>
                                    {trains.map(t => <option key={t.trainId} value={t.trainId}>{t.trainName}</option>)}
                                </select>
                            </div>
                            <div className="form-group"><label>Number</label><input type="text" name="carriageNumber" required /></div>
                            <div className="form-group">
                                <label>Class Type</label>
                                <select name="classType" required>
                                    <option value="First Class">First Class</option>
                                    <option value="Second Class">Second Class</option>
                                    <option value="Third Class">Third Class</option>
                                </select>
                            </div>
                            <div className="form-group"><label>Capacity</label><input type="number" name="capacity" required defaultValue="40" /></div>
                            <div className="modal-actions">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowAddModal(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary" style={{flex: 1}}>Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};
