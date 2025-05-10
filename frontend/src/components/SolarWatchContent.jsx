function SolarWatchContent({data}) {
    return (
        <div className="space-y-4">
            <div className="card bg-base-200 shadow-md">
                <div className="card-body ">
                    <p><strong>City:</strong> {data.cityName}</p>
                    <p><strong>Country:</strong> {data.country}</p>
                    <p><strong>Date:</strong> {data.date}</p>
                    <p><strong><u>Date time is in UTC</u></strong></p>
                    <p><strong>Sunrise:</strong> {data.sunrise}</p>
                    <p><strong>Sunset:</strong> {data.sunset}</p>
                </div>
            </div>
        </div>
    )
}

export default SolarWatchContent;