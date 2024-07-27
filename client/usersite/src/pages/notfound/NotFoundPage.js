import "./NotFoundPage.css";

function NotFoundPage() {
  return (
    <div className="not-found-page">
      <h1>404 - Not Found!</h1>
      <p>Sorry, the page you are looking for does not exist.</p>
      <p>Go to home page: <a href="/intro">Home</a></p>
    </div>
  );
}

export default NotFoundPage;
